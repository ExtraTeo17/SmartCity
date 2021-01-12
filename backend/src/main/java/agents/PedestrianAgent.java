package agents;

import agents.abstractions.AbstractAgent;
import agents.utilities.LoggerLevel;
import agents.utilities.MessageParameter;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import events.web.bike.BikeAgentCreatedEvent;
import events.web.pedestrian.PedestrianAgentDeadEvent;
import events.web.pedestrian.PedestrianAgentEnteredBusEvent;
import events.web.pedestrian.PedestrianAgentLeftBusEvent;
import events.web.pedestrian.PedestrianAgentUpdatedEvent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Properties;
import org.jetbrains.annotations.NotNull;
import routing.RoutingConstants;
import routing.abstractions.IRouteGenerator;
import routing.core.IGeoPosition;
import routing.core.Position;
import routing.nodes.LightManagerNode;
import routing.nodes.RouteNode;
import routing.nodes.StationNode;
import smartcity.ITimeProvider;
import smartcity.SmartCityAgent;
import smartcity.config.abstractions.IChangeTransportConfigContainer;
import smartcity.task.abstractions.ITaskProvider;
import vehicles.Bike;
import vehicles.Pedestrian;
import vehicles.TestPedestrian;
import vehicles.enums.DrivingState;

import java.time.LocalTime;
import java.util.List;

import static agents.AgentConstants.DEFAULT_BLOCK_ON_ERROR;
import static agents.message.MessageManager.createMessage;
import static agents.message.MessageManager.createProperties;
import static agents.utilities.BehaviourWrapper.wrapErrors;
import static smartcity.config.StaticConfig.USE_BATCHED_UPDATES;

/**
 * The number of PedestrianAgent agents in the system is configurable from the GUI. It is an agent,
 * which behaves similarly to the car agents in terms of travelling manner (from point A to point B)
 * and in terms of communication with the PedestrianAgent agents throughout its route. Its core
 * responsibility is to communicate with the bus station agent to inform it about the need of travelling
 * with a certain bus line and wait until proposal to enter the bus.
 */
public class PedestrianAgent extends AbstractAgent {
    public static final String name = PedestrianAgent.class.getSimpleName().replace("Agent", "");
    public static final long NANO_IN_MILLISECONDS = 1_000_000L;


    private final IRouteGenerator router;
    private final ITaskProvider taskProvider;
    private final IChangeTransportConfigContainer troublePointsConfigContainer;

    private Pedestrian pedestrian;
    private List<RouteNode> arrivingRouteToClosestStation;
    private List<RouteNode> bikeRoute;

    PedestrianAgent(int agentId,
                    Pedestrian pedestrian,
                    ITimeProvider timeProvider,
                    ITaskProvider taskProvider,
                    EventBus eventBus,
                    IRouteGenerator router,
                    IChangeTransportConfigContainer troublePointsConfigContainer) {
        super(agentId, pedestrian.getVehicleType(), timeProvider, eventBus);
        this.taskProvider = taskProvider;
        this.pedestrian = pedestrian;
        this.router = router;
        this.troublePointsConfigContainer = troublePointsConfigContainer;
    }


    /**
     * Tell whether the pedestrian is currently commuting via a bus or walking by foot.
     *
     * @return true if the pedestrian is travelling in the bus, false otherwise
     */
    public boolean isInBus() {
        return DrivingState.IN_BUS == pedestrian.getState();
    }


    @Override
    protected void setup() {
        informWhichBusLine();
        informLightManager(pedestrian);

        pedestrian.setState(DrivingState.MOVING);
        Behaviour move = new TickerBehaviour(this, RoutingConstants.STEP_CONSTANT / pedestrian.getSpeed()) {
            @Override
            public void onTick() {
                if (pedestrian.isAtTrafficLights()) {
                    switch (pedestrian.getState()) {
                        case MOVING:
                            var light = pedestrian.getCurrentTrafficLightNode();
                            ACLMessage msg = createMessageById(ACLMessage.REQUEST_WHEN, LightManagerAgent.name,
                                    light.getLightManagerId());
                            Properties properties = createProperties(MessageParameter.PEDESTRIAN);
                            properties.setProperty(MessageParameter.ADJACENT_OSM_WAY_ID,
                                    Long.toString(pedestrian.getAdjacentOsmWayId()));
                            msg.setAllUserDefinedParameters(properties);
                            send(msg);
                            pedestrian.setState(DrivingState.WAITING_AT_LIGHT);
                            print("Asking LightManager" + light.getLightManagerId() + " for right to passage.");
                            break;

                        case WAITING_AT_LIGHT:

                            break;
                        case PASSING_LIGHT:
                            print("Passing the light.");
                            move();
                            pedestrian.setState(DrivingState.MOVING);
                            break;
                    }
                } else if (pedestrian.isAtStation()) {
                    switch (pedestrian.getState()) {
                        case MOVING:
                            StationNode station = pedestrian.getStartingStation();
                            ACLMessage msg = createMessageById(ACLMessage.REQUEST_WHEN, StationAgent.name, station.getAgentId());
                            Properties properties = createProperties(MessageParameter.PEDESTRIAN);
                            properties.setProperty(MessageParameter.DESIRED_OSM_STATION_ID, String.valueOf(pedestrian.getTargetStation().getOsmId()));
                            properties.setProperty(MessageParameter.ARRIVAL_TIME, timeProvider.getCurrentSimulationTime()
                                    .toString());
                            msg.setAllUserDefinedParameters(properties);
                            send(msg);

                            print("Send REQUEST_WHEN to Station" + station.getAgentId());

                            pedestrian.setState(DrivingState.WAITING_AT_STATION);
                            break;
                        case WAITING_AT_STATION:
                            // waiting for bus...

                            break;
                        case IN_BUS:
                            // traveling inside a bus

                            break;
                        case PASSING_STATION:
                            move();
                            pedestrian.setState(DrivingState.MOVING);
                            break;
                    }
                } else if (pedestrian.isAtDestination()) {
                    pedestrian.setState(DrivingState.AT_DESTINATION);
                    print("Reached destination.");
                    sendMessageAboutReachingDestinationToSmartCityAgent();
                    doDelete();
                } else if (!pedestrian.isTroubled()) {
                    move();
                }
            }
        };

        Behaviour communication = new CyclicBehaviour() {

            private long bikeTimeMilliseconds;
            private StationNode expectedNewStationNode;
            private IGeoPosition currentPosition;

            @Override
            @SuppressWarnings("DuplicatedCode")
            public void action() {
                ACLMessage rcv = receive();
                if (rcv == null) {
                    block();
                    return;
                }

                String type = rcv.getUserDefinedParameter(MessageParameter.TYPE);
                if (type == null) {
                    block(DEFAULT_BLOCK_ON_ERROR);
                    logTypeError(rcv);
                    return;
                }

                switch (type) {
                    case MessageParameter.LIGHT:
                        if (rcv.getPerformative() == ACLMessage.REQUEST) {
                            if (pedestrian.getState() == DrivingState.WAITING_AT_LIGHT) {
                                ACLMessage response = createMessage(ACLMessage.AGREE, rcv.getSender());
                                Properties properties = createProperties(MessageParameter.PEDESTRIAN);
                                properties.setProperty(MessageParameter.ADJACENT_OSM_WAY_ID,
                                        Long.toString(pedestrian.getAdjacentOsmWayId()));
                                response.setAllUserDefinedParameters(properties);
                                send(response);

                                if (pedestrian.findNextStop() instanceof LightManagerNode) {
                                    informLightManager(pedestrian);
                                }
                                pedestrian.setState(DrivingState.PASSING_LIGHT);
                            }
                        }
                        break;
                    case MessageParameter.STATION:
                        if (rcv.getPerformative() == ACLMessage.REQUEST) {
                            ACLMessage response = createMessage(ACLMessage.AGREE, rcv.getSender());
                            var properties = createProperties(MessageParameter.PEDESTRIAN);
                            properties.setProperty(MessageParameter.DESIRED_OSM_STATION_ID, String.valueOf(pedestrian.getTargetStation().getOsmId()));
                            response.setAllUserDefinedParameters(properties);
                            send(response);

                            ACLMessage msg = createMessage(ACLMessage.REQUEST_WHEN,
                                    rcv.getUserDefinedParameter(MessageParameter.BUS_AGENT_NAME));
                            properties = createProperties(MessageParameter.PEDESTRIAN);
                            properties.setProperty(MessageParameter.STATION_ID, String.valueOf(pedestrian.getTargetStation()
                                    .getAgentId()));
                            msg.setAllUserDefinedParameters(properties);
                            enterBus();
                            send(msg);

                            while (!pedestrian.isAtStation() && !pedestrian.isAtDestination()) {
                                pedestrian.move();
                            }
                        } else if (rcv.getPerformative() == ACLMessage.INFORM) {
                            handleCrashOfTheBus(rcv);
                        }
                        break;
                    case MessageParameter.BUS:
                        if (rcv.getPerformative() == ACLMessage.REQUEST) {
                            ACLMessage response = createMessage(ACLMessage.AGREE, rcv.getSender());

                            Properties properties = createProperties(MessageParameter.PEDESTRIAN);
                            properties.setProperty(MessageParameter.STATION_ID, String.valueOf(pedestrian.getTargetStation()
                                    .getAgentId()));
                            response.setAllUserDefinedParameters(properties);
                            send(response);

                            if (!pedestrian.isAtDestination()) {
                                quitBus();
                                pedestrian.setState(DrivingState.PASSING_STATION);
                            }
                            informLightManager(pedestrian);
                        } else if (rcv.getPerformative() == ACLMessage.INFORM) {
                            handleCrashOfTheBus(rcv);
                        }
                        break;
                    case MessageParameter.BUS_MANAGER:
                        if (rcv.getPerformative() == ACLMessage.INFORM) {
                            if (rcv.getUserDefinedParameter(MessageParameter.EVENT).equals(MessageParameter.TROUBLE)) {
                                handleTrouble(rcv);
                            } else if (rcv.getUserDefinedParameter(MessageParameter.EVENT).equals(MessageParameter.START)) {
                                getNextStation(rcv.getUserDefinedParameter(MessageParameter.BUS_LINE));
                            }
                        }
                }
            }

            private void handleCrashOfTheBus(ACLMessage rcv) {
                pedestrian.setTroubled(true);
                logger.info("Get info about trouble from bus");

                expectedNewStationNode = parseCrashMessageFromBus(rcv);

                var troublePoint = Position.of(rcv.getUserDefinedParameter(MessageParameter.TROUBLE_LAT),
                        rcv.getUserDefinedParameter(MessageParameter.TROUBLE_LON));
                IGeoPosition nextClosestStationPosition = Position.of(String.valueOf(expectedNewStationNode.getLat()),
                        String.valueOf(expectedNewStationNode.getLng()));
                if (troublePoint.equals(nextClosestStationPosition)) {
                    currentPosition = pedestrian.getPosition();
                } else {
                    currentPosition = troublePoint;
                }


                LocalTime arrivalTime = LocalTime.of(0, 0, 0, 0);
                if (currentPosition != nextClosestStationPosition) {

                            arrivalTime = computeArrivalTime(currentPosition, nextClosestStationPosition,
                                    expectedNewStationNode);
                        }
                sendMessageToBusManager(rcv, arrivalTime.toString());

                if (expectedNewStationNode.equals(pedestrian.getStationFinish())) {
                    performMetamorphosisToBike();
                    return;
                }

                if (troublePointsConfigContainer.isTransportChangeStrategyActive()) {
                    var route = pedestrian.getUniformRoute();
                    computeBikeTime(currentPosition, route.get(route.size() - 1));
                }
            }

            private void sendMessageToBusManager(ACLMessage rcv, String arrivalTime) {
                ACLMessage messageToBusManager = createMessage(ACLMessage.INFORM, BusManagerAgent.NAME);
                messageToBusManager.addUserDefinedParameter(MessageParameter.ARRIVAL_TIME, arrivalTime);

                messageToBusManager.addUserDefinedParameter(MessageParameter.STATION_FROM_ID,
                        String.valueOf(expectedNewStationNode.getOsmId()));

                messageToBusManager.addUserDefinedParameter(MessageParameter.EVENT,
                        MessageParameter.TROUBLE);
                messageToBusManager.addUserDefinedParameter(MessageParameter.STATION_TO_ID,
                        String.valueOf(pedestrian.getTargetStation().getOsmId()));
                messageToBusManager.addUserDefinedParameter(MessageParameter.BUS_LINE,
                        rcv.getUserDefinedParameter(MessageParameter.BUS_LINE));
                messageToBusManager.addUserDefinedParameter(MessageParameter.BRIGADE,
                        rcv.getUserDefinedParameter(MessageParameter.BRIGADE));

                send(messageToBusManager);
            }

            private StationNode parseCrashMessageFromBus(ACLMessage rcv) {

                return new StationNode(rcv.getUserDefinedParameter(MessageParameter.LAT_OF_NEXT_CLOSEST_STATION),
                        rcv.getUserDefinedParameter(MessageParameter.LON_OF_NEXT_CLOSEST_STATION),
                        rcv.getUserDefinedParameter(MessageParameter.DESIRED_OSM_STATION_ID),
                        rcv.getUserDefinedParameter(MessageParameter.AGENT_ID_OF_NEXT_CLOSEST_STATION));

            }

            private void handleTrouble(ACLMessage rcv) {
                logger.info("Got Inform message from BUS MANAGER");
                long timeBetweenArrivalAtStationAndDesiredStation = Long.parseLong(rcv.getUserDefinedParameter(
                        MessageParameter.TIME_BETWEEN_PEDESTRIAN_AT_STATION_ARRIVAL_AND_REACHING_DESIRED_STOP));
                long busTimeMilliseconds = (pedestrian.getMillisecondsOnRoute(arrivingRouteToClosestStation))
                        + (timeBetweenArrivalAtStationAndDesiredStation * 1000)
                        + (pedestrian.getMillisecondsOnRoute(pedestrian.getUniformRoute()));
                if (troublePointsConfigContainer.isTransportChangeStrategyActive()) {
                    handleTransportChangeWithStrategy(rcv, busTimeMilliseconds);
                } else {
                    handleTransportChangeWithoutStrategy(rcv, busTimeMilliseconds);
                }
            }

            private void handleTransportChangeWithStrategy(final ACLMessage rcv, final long busTimeMilliseconds) {
                logger.info("Transport change strategy is active: Bike time in milliseconds: " + bikeTimeMilliseconds + " vs bus time in milliseconds: "
                        + busTimeMilliseconds);
                if (bikeTimeMilliseconds > busTimeMilliseconds) {
                    logger.info("Choose bus because bike time in milliseconds: " + bikeTimeMilliseconds
                            + " vs bus time in milliseconds: " + busTimeMilliseconds);
                    restartAgentWithNewBusLine(arrivingRouteToClosestStation, rcv.getUserDefinedParameter(MessageParameter.BUS_LINE));
                } else {
                    logger.info("Choose bike because bike time in milliseconds: " + bikeTimeMilliseconds
                            + " vs bus time in milliseconds: " + busTimeMilliseconds);
                    performMetamorphosisToBike();
                }
            }

            private void handleTransportChangeWithoutStrategy(final ACLMessage rcv, final long busTimeMilliseconds) {
                logger.info("Transport change strategy is not active. Bus time in milliseconds: "
                        + busTimeMilliseconds + " -- seeking another bus");
                restartAgentWithNewBusLine(arrivingRouteToClosestStation, rcv.getUserDefinedParameter(MessageParameter.BUS_LINE));
            }

            private void performMetamorphosisToBike() {
                logger.info("Perform metamorphosis to bike");

                var isTestPedestrian = pedestrian instanceof TestPedestrian;
                if (isTestPedestrian) {
                    eventBus.register(this);
                } else {
                    pedestrian.setState(DrivingState.AT_DESTINATION);
                    sendMessageAboutReachingDestinationToSmartCityAgent();
                    myAgent.doDelete();
                    logger.info("Kill pedestrian agent (metamorphosis to bike)");
                }
                taskProvider.getCreateBikeTask(currentPosition, pedestrian.getEndPosition(),
                        isTestPedestrian).run();
            }

            @Subscribe
            public void handle(BikeAgentCreatedEvent e) {
                if (e.isTestBike) {
                    eventBus.unregister(this);
                    pedestrian.setState(DrivingState.AT_DESTINATION);
                    sendMessageAboutReachingDestinationToSmartCityAgent(String.valueOf(e.agentId));
                    myAgent.doDelete();
                    logger.info("Kill pedestrian agent (metamorphosis to bike)");
                }
            }

            private void restartAgentWithNewBusLine(List<RouteNode> arrivingRouteToClosestStation, String busLine) {
                pedestrian = new Pedestrian(pedestrian.getAgentId(),
                        arrivingRouteToClosestStation,
                        arrivingRouteToClosestStation,
                        pedestrian.getDisplayRouteAfterBus(),
                        pedestrian.getDisplayRouteAfterBus(),
                        expectedNewStationNode,
                        pedestrian.getStationFinish(),
                        timeProvider,
                        taskProvider);
                getNextStation(busLine);
                informLightManager(pedestrian);
                pedestrian.setTroubled(false);
                pedestrian.setState(DrivingState.MOVING);


                if (!pedestrian.isAtStation()) {
                    quitBus(false);
                }
            }

            private void computeBikeTime(IGeoPosition pointA, IGeoPosition pointB) {
                bikeRoute = router.generateRouteInfo(pointA, pointB, "bike");
                int firstIndex = 0;
                int bikeSpeed = Bike.DEFAULT_SPEED;
                bikeTimeMilliseconds = pedestrian.getMillisecondsOnRoute(bikeRoute, firstIndex, bikeSpeed);
            }

            private LocalTime computeArrivalTime(IGeoPosition pointA, IGeoPosition pointB, StationNode desiredOsmStation) {
                LocalTime now = timeProvider.getCurrentSimulationTime().toLocalTime();

                arrivingRouteToClosestStation = router.generateRouteForPedestrians(pointA, pointB, null,
                		desiredOsmStation);
                return now.plusNanos(pedestrian.getMillisecondsOnRoute(arrivingRouteToClosestStation) * NANO_IN_MILLISECONDS);

            }
        };
        var onError = createErrorConsumer(new PedestrianAgentDeadEvent(this.getId(),
                this.pedestrian.getUniformRouteSize(), null));
        addBehaviour(wrapErrors(move, onError));
        addBehaviour(wrapErrors(communication, onError));
    }

    private void sendMessageAboutReachingDestinationToSmartCityAgent() {
        sendMessageAboutReachingDestinationToSmartCityAgent("");
    }

    private void sendMessageAboutReachingDestinationToSmartCityAgent(@NotNull String testBikeAgentId) {
        ACLMessage msg = createMessage(ACLMessage.INFORM, SmartCityAgent.name);
        Properties prop = createProperties(MessageParameter.PEDESTRIAN);
        prop.setProperty(MessageParameter.AT_DESTINATION, String.valueOf(Boolean.TRUE));
        prop.setProperty(MessageParameter.TEST_BIKE_AGENT_ID, testBikeAgentId);
        msg.setAllUserDefinedParameters(prop);
        send(msg);
    }


    private void getNextStation(final String busLine) {
        // finds next station and announces his arrival
        StationNode nextStation = pedestrian.findNextStation();
        pedestrian.setState(DrivingState.MOVING);
        if (nextStation != null) {
            ACLMessage msg = createMessageById(ACLMessage.INFORM, StationAgent.name, nextStation.getAgentId());
            Properties properties = createProperties(MessageParameter.PEDESTRIAN);
            var currentTime = timeProvider.getCurrentSimulationTime();
            var predictedTime = currentTime.plusNanos(pedestrian.getMillisecondsToNextStation() * NANO_IN_MILLISECONDS);
            properties.setProperty(MessageParameter.ARRIVAL_TIME, predictedTime.toString());
            properties.setProperty(MessageParameter.BUS_LINE, busLine);
            msg.setAllUserDefinedParameters(properties);

            send(msg);
            print("Sent INFORM to Station");
        }
    }

    private void informWhichBusLine() {
        logger.info("Send inform about bus_line to to Bus");

        ACLMessage msg = createMessage(ACLMessage.INFORM, BusManagerAgent.NAME);

        var currentTime = timeProvider.getCurrentSimulationTime();
        var predictedTime = currentTime.plusNanos(pedestrian.getMillisecondsToNextStation() * NANO_IN_MILLISECONDS).toLocalTime();
        msg.addUserDefinedParameter(MessageParameter.ARRIVAL_TIME, predictedTime.toString());
        msg.addUserDefinedParameter(MessageParameter.STATION_FROM_ID, String.valueOf(pedestrian.getStartingStation().getOsmId()));
        msg.addUserDefinedParameter(MessageParameter.STATION_TO_ID, String.valueOf(pedestrian.getStationFinish().getOsmId()));
        msg.addUserDefinedParameter(MessageParameter.EVENT, MessageParameter.START);
        send(msg);
    }

    private void move() {
        pedestrian.move();
        if (!USE_BATCHED_UPDATES) {
            eventBus.post(new PedestrianAgentUpdatedEvent(this.getId(), pedestrian.getPosition()));
        }
    }

    private void enterBus() {
        print("Enter bus", LoggerLevel.DEBUG);
        pedestrian.setState(DrivingState.IN_BUS);
        eventBus.post(new PedestrianAgentEnteredBusEvent(this.getId()));
    }

    private void quitBus() {
        quitBus(true);
    }

    private void quitBus(boolean shouldShowRoute) {
        print("Quit bus", LoggerLevel.DEBUG);
        pedestrian.move();
        eventBus.post(new PedestrianAgentLeftBusEvent(this.getId(), pedestrian.getPosition(), shouldShowRoute));
    }

    public IGeoPosition getPosition() {
        return pedestrian.getPosition();
    }

    public Pedestrian getPedestrian() {
        return pedestrian;
    }

    @Override
    protected final String getAdjacentIdParameter(final LightManagerNode node) {
        return String.valueOf(node.getCrossingOsmId1());
    }
}
