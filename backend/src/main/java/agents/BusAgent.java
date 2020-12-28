package agents;

import agents.abstractions.AbstractAgent;
import agents.utilities.LoggerLevel;
import agents.utilities.MessageParameter;
import com.google.common.eventbus.EventBus;
import events.web.bus.BusAgentCrashedEvent;
import events.web.bus.BusAgentDeadEvent;
import events.web.bus.BusAgentUpdatedEvent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Properties;
import jade.wrapper.AgentState;
import routing.RoutingConstants;
import routing.core.IGeoPosition;
import routing.nodes.LightManagerNode;
import routing.nodes.RouteNode;
import routing.nodes.StationNode;
import smartcity.ITimeProvider;
import smartcity.SmartCityAgent;
import smartcity.config.abstractions.IChangeTransportConfigContainer;
import utilities.ConditionalExecutor;
import utilities.Siblings;
import vehicles.Bus;
import vehicles.enums.DrivingState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static agents.AgentConstants.DEFAULT_BLOCK_ON_ERROR;
import static agents.message.MessageManager.createMessage;
import static agents.message.MessageManager.createProperties;
import static agents.utilities.BehaviourWrapper.wrapErrors;
import static smartcity.config.StaticConfig.USE_BATCHED_UPDATES;

/**
 * The main aim of Bus agent if to circulate between point A and B and
 * stop at bus stops in order to gather all Traveler agents. As soon as
 * the bus reaches a stop, it sends information to the next one about its
 * upcoming arrival. When he is at the stop, he informs the
 * {@link StationAgent} about it and waits for the command to depart.
 * In addition, when boarding, passengers send him information where they are
 * going. This allows the bus to control the number of passengers and inform
 * them about destination stop. Besides that, on the road of Bus can appear only
 * one type of obstacles - traffic jams.
 */
public class BusAgent extends AbstractAgent {
    public static final String name = BusAgent.class.getSimpleName().replace("Agent", "");

    private final ITimeProvider timeProvider;
    private final Bus bus;
    private final IChangeTransportConfigContainer configContainer;
    private RouteNode troublePoint;
    private int numberOfPassedStations = 0;

    BusAgent(int busId, Bus bus,
             ITimeProvider timeProvider,
             EventBus eventBus,
             IChangeTransportConfigContainer configContainer) {
        super(busId, name, timeProvider, eventBus);
        this.timeProvider = timeProvider;
        this.bus = bus;
        this.configContainer = configContainer;
    }

    @Override
    protected void setup() {
        informNextStation();
        var firstStationOpt = bus.getCurrentStationNode();
        if (firstStationOpt.isEmpty()) {
            print("No stations on route!", LoggerLevel.ERROR);
            return;
        }

        print("Bus start: " + bus.getSuperExtraString());

        var firstStation = firstStationOpt.get();
        print("Started at station " + firstStation.getAgentId() + ".");
        bus.setState(DrivingState.MOVING);

        // TODO: Executed each x = 3600 / bus.getSpeed() = 3600m / (40 * TIME_SCALE) = 3600 / 400 = 9ms
        //   Maybe decrease the interval? - I don't think processor can keep up with all of this.
        Behaviour move = new TickerBehaviour(this, RoutingConstants.STEP_CONSTANT / bus.getSpeed()) {
            @Override
            public void onTick() {
                if (bus.isAtTrafficLights()) {
                    switch (bus.getState()) {
                        case MOVING:
                            var light = bus.getCurrentTrafficLightNode();
                            ACLMessage msg = createMessageById(ACLMessage.REQUEST_WHEN, LightManagerAgent.name,
                                    light.getLightManagerId());
                            // TODO: Should it be Vehicle?
                            Properties properties = createProperties(MessageParameter.VEHICLE);
                            properties.setProperty(MessageParameter.ADJACENT_OSM_WAY_ID, Long.toString(bus.getAdjacentOsmWayId()));
                            msg.setAllUserDefinedParameters(properties);
                            send(msg);
                            bus.setState(DrivingState.WAITING_AT_LIGHT);

                            break;
                        case WAITING_AT_LIGHT:

                            break;
                        case PASSING_LIGHT:
                            print("Passing the light.");
                            move();
                            bus.setState(DrivingState.MOVING);
                            break;
                    }
                }
                else if (bus.isAtStation()) {
                    switch (bus.getState()) {
                        case MOVING:
                            var stationOpt = bus.getCurrentStationNode();
                            if (stationOpt.isEmpty()) {
                                logger.error("Bus in not at station, but function returned that it is.");
                                return;
                            }
                            var station = stationOpt.get();
                            List<String> passengerNames = bus.getPassengers(station.getAgentId());

                            if (passengerNames.size() > 0) {
                                ACLMessage leave = createMessage(ACLMessage.REQUEST, passengerNames);
                                Properties properties = createProperties(MessageParameter.BUS);
                                properties.setProperty(MessageParameter.STATION_ID, String.valueOf(station.getAgentId()));
                                leave.setAllUserDefinedParameters(properties);
                                send(leave);
                            }

                            ACLMessage msg = createMessageById(ACLMessage.REQUEST_WHEN, StationAgent.name, station.getAgentId());
                            Properties properties = createProperties(MessageParameter.BUS);

                            var timeOnStation = bus.getTimeOnStation(station.getOsmId());
                            timeOnStation.ifPresent(time -> properties.setProperty(MessageParameter.SCHEDULE_ARRIVAL, time
                                    .toString()));
                            properties.setProperty(MessageParameter.ARRIVAL_TIME, timeProvider.getCurrentSimulationTime()
                                    .toString());
                            msg.setAllUserDefinedParameters(properties);
                            send(msg);

                            bus.setState(DrivingState.WAITING_AT_STATION);
                            break;
                        case WAITING_AT_STATION:
                            // waiting for passengers...

                            // if you want to skip waiting (for tests) use this:
                            // bus.setState(DrivingState.PASSING_STATION);
                            break;
                        case PASSING_STATION:
                            RouteNode node = bus.findNextStop();
                            if (node instanceof LightManagerNode) {
                                informLightManager(bus);
                            }
                            informNextStation();
                            numberOfPassedStations++;
                            bus.setState(DrivingState.MOVING);
                            move();
                            break;
                    }
                }
                else if (bus.isAtDestination()) {
                    bus.setState(DrivingState.AT_DESTINATION);
                    logger.info("isAtDestination");
                    ACLMessage msg = createMessage(ACLMessage.INFORM, SmartCityAgent.name);
                    Properties prop = createProperties(MessageParameter.BUS);
                    prop.setProperty(MessageParameter.AT_DESTINATION, String.valueOf(Boolean.TRUE));
                    msg.setAllUserDefinedParameters(prop);
                    send(msg);
                    doDelete();
                }
                else {
                    move();
                }
            }
        };

        Behaviour communication = new CyclicBehaviour() {
            @SuppressWarnings("DuplicatedCode")
            @Override
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
                            if (bus.getState() == DrivingState.WAITING_AT_LIGHT) {
                                ACLMessage response = createMessage(ACLMessage.AGREE, rcv.getSender());
                                // TODO: Should it be Vehicle?
                                Properties properties = createProperties(MessageParameter.VEHICLE);
                                properties.setProperty(MessageParameter.ADJACENT_OSM_WAY_ID, Long.toString(bus.getAdjacentOsmWayId()));
                                response.setAllUserDefinedParameters(properties);
                                send(response);
                                if (bus.findNextStop() instanceof LightManagerNode) {
                                    informLightManager(bus);
                                }
                                bus.setState(DrivingState.PASSING_LIGHT);
                            }
                        }
                        break;
                    case MessageParameter.STATION:
                        if (rcv.getPerformative() == ACLMessage.REQUEST) {
                            if (bus.getState() == DrivingState.WAITING_AT_STATION) {
                                ACLMessage response = createMessage(ACLMessage.AGREE, rcv.getSender());

                                Properties properties = createProperties(MessageParameter.BUS);

                                response.setAllUserDefinedParameters(properties);
                                send(response);
                                informNextStation();
                                bus.setState(DrivingState.PASSING_STATION);
                            }
                        }
                        else if (rcv.getPerformative() == ACLMessage.AGREE) {
                            logger.info("GOT AGREE from station");
                            bus.setState(DrivingState.WAITING_AT_STATION);
                        }
                        break;
                    case MessageParameter.PEDESTRIAN:
                        switch (rcv.getPerformative()) {
                            case ACLMessage.REQUEST_WHEN:
                                int stationId =
                                        Integer.parseInt(rcv.getUserDefinedParameter(MessageParameter.STATION_ID));
                                bus.addPassengerToStation(stationId, rcv.getSender().getLocalName());

                                ACLMessage response = createMessage(ACLMessage.AGREE, rcv.getSender());
                                Properties properties = createProperties(MessageParameter.BUS);
                                response.setAllUserDefinedParameters(properties);
                                send(response);
                                break;
                            case ACLMessage.AGREE:
                                stationId = Integer.parseInt(rcv.getUserDefinedParameter(MessageParameter.STATION_ID));
                                if (bus.removePassengerFromStation(stationId, rcv.getSender().getLocalName())) {
                                    print("Passengers: " + bus.getPassengersCount());
                                }
                                else {
                                    print("Removing passenger failed");
                                }
                                break;
                        }

                        break;
                }
            }
        };

        var onError = createErrorConsumer(new BusAgentDeadEvent(this.getId()));
        addBehaviour(wrapErrors(move, onError));
        addBehaviour(wrapErrors(communication, onError));


        if (configContainer.shouldGenerateBusFailures()) {
            Behaviour troubleGenerator = new TickerBehaviour(this, 8000) {

                @Override
                public void onTick() {
                    if (configContainer.wasBusCrashGeneratedOnce()) {
                        logger.trace("Bus crash has already been generated once");
                        stop();
                        return;
                    }
                    configContainer.setBusCrashGeneratedOnce(true);

                    int index = bus.getMoveIndex();
                    var trouble = bus.getUniformRoute().get(index);
                    troublePoint = new RouteNode(trouble.getLat(), trouble.getLng(),
                            trouble.getInternalEdgeId());
                    //send message to boss Agent/ maybe not so important in case of buses
                    var timeOfCrash = timeProvider.getCurrentSimulationTime();
                    sendMessageAboutCrashTroubleToTroubleManager(timeOfCrash);
                    sendMessageAboutCrashTroubleToIncomingStations(timeOfCrash);
                    sendMessageAboutCrashTroubleToPedestrians(timeOfCrash);
                    sendMessageAboutCrashTroubleToBusManager(timeOfCrash);
                    logger.info("Generated trouble");

                    eventBus.post(new BusAgentCrashedEvent(getId()));
                    doDelete();
                }

                private void sendMessageAboutCrashTroubleToBusManager(LocalDateTime timeOfCrash) {
                    ACLMessage msg = createMessageAboutCrash(BusManagerAgent.NAME, false, timeOfCrash);
                    logger.info("Send message about crash to BusManager ");
                    send(msg);
                }

                private void sendMessageAboutCrashTroubleToIncomingStations(LocalDateTime timeOfCrash) {

                    var stations = bus.getStationNodesOnRoute();
                    for (int i = numberOfPassedStations; i < stations.size(); i++) {
                        ACLMessage msg = createMessageAboutCrash(StationAgent.name + stations.get(i).getAgentId(), false, timeOfCrash);
                        logger.info("Send message about crash to incoming stations ");
                        send(msg);
                    }
                }

                private void sendMessageAboutCrashTroubleToPedestrians(LocalDateTime timeOfCrash) {
                    for (String pedestrian : bus.getAllPassengers()) {
                        ACLMessage msg = createMessageAboutCrash(pedestrian, false, timeOfCrash);
                        logger.info("Send message about crash to pedestrian: " + pedestrian);
                        send(msg);
                    }
                }

                private ACLMessage createMessageAboutCrash(String agentName, boolean isTroubleManager, LocalDateTime timeOfCrash) {

                    ACLMessage msg = createMessage(ACLMessage.INFORM, agentName);
                    Properties properties = createProperties(MessageParameter.BUS);
                    properties.setProperty(MessageParameter.TROUBLE, MessageParameter.SHOW);
                    properties.setProperty(MessageParameter.TYPEOFTROUBLE, MessageParameter.CRASH);
                    properties.setProperty(MessageParameter.TROUBLE, MessageParameter.SHOW);
                    properties.setProperty(MessageParameter.CRASH_TIME, timeOfCrash.toLocalTime().toString());
                    properties.setProperty(MessageParameter.TROUBLE_LAT, Double.toString(troublePoint.getLat()));
                    properties.setProperty(MessageParameter.TROUBLE_LON, Double.toString(troublePoint.getLng()));
                    if (!isTroubleManager) {
                        properties.setProperty(MessageParameter.DESIRED_OSM_STATION_ID, ((StationNode) bus.findNextStop()).getOsmId() + "");
                        properties.setProperty(MessageParameter.AGENT_ID_OF_NEXT_CLOSEST_STATION, ((StationNode) bus.findNextStop()).getAgentId() + "");
                        //maybe not needed
                        properties.setProperty(MessageParameter.LAT_OF_NEXT_CLOSEST_STATION, bus.findNextStop().getLat() + "");
                        properties.setProperty(MessageParameter.LON_OF_NEXT_CLOSEST_STATION, bus.findNextStop().getLng() + "");

                        properties.setProperty(MessageParameter.BUS_LINE, getLine());
                        properties.setProperty(MessageParameter.BRIGADE, bus.getBrigade());
                    }
                    msg.setAllUserDefinedParameters(properties);
                    return msg;
                }

                private void sendMessageAboutCrashTroubleToTroubleManager(LocalDateTime timeOfCrash) {

                    ACLMessage msg = createMessageAboutCrash(TroubleManagerAgent.name, true, timeOfCrash);
                    logger.info("Send message about crash to Trouble Manager ");
                    send(msg);
                }

            };

            addBehaviour(wrapErrors(troubleGenerator, onError));
        }
    }

    public void move() {
        bus.move();
        if (!USE_BATCHED_UPDATES) {
            eventBus.post(new BusAgentUpdatedEvent(this.getId(), bus.getPosition()));
        }
    }

    public IGeoPosition getPosition() {
        return bus.getPosition();
    }

    private void informNextStation() {
        // finds next station and announces his arrival
        var stationOpt = bus.findNextStation();
        if (stationOpt.isPresent()) {
            var station = stationOpt.get();
            var stationId = station.getAgentId();
            ACLMessage msg = createMessageById(ACLMessage.INFORM, StationAgent.name, stationId);
            var properties = createProperties(MessageParameter.BUS);
            var currentTime = timeProvider.getCurrentSimulationTime();
            var predictedTime = currentTime.plusNanos(bus.getMillisecondsToNextStation() * 1_000_000L);
            properties.setProperty(MessageParameter.ARRIVAL_TIME, predictedTime.toString());
            properties.setProperty(MessageParameter.BUS_LINE, bus.getLine());

            var osmId = station.getOsmId();
            var timeOnStation = bus.getTimeOnStation(osmId);
            if (timeOnStation.isPresent()) {
                properties.setProperty(MessageParameter.SCHEDULE_ARRIVAL, timeOnStation.get().toString());
            }
            else {
                ConditionalExecutor.debug(this::logAllStations);
            }

            msg.setAllUserDefinedParameters(properties);
            send(msg);
        }
    }

    private void logAllStations() {
        print("Printing station nodes: ", LoggerLevel.DEBUG);
        var stations = bus.getStationNodesOnRoute();
        for (int i = 0; i < stations.size(); ++i) {
            var station = stations.get(i);
            var osmId = station.getOsmId();
            var stationId = station.getAgentId();
            var timeOnStation = bus.getTimeOnStation(osmId);
            var timeString = timeOnStation.map(LocalDateTime::toString).orElse("");
            print(i + ": [" + osmId + "][" + stationId + "] on '" + timeString + "'", LoggerLevel.DEBUG);
        }
        print("Printing station nodes finished.", LoggerLevel.DEBUG);
    }

    public Bus getBus() {
        return bus;
    }

    private String getLine() {
        return bus.getLine();
    }

    // TODO: Fix situation where bus route contains only one station and pedestrians tries to choose two
    public final Optional<Siblings<StationNode>> getTwoSubsequentStations(final Random random) {

        List<StationNode> stationsOnRoute = bus.getStationNodesOnRoute();
        if (stationsOnRoute.size() <= 1) {
            return Optional.empty();
        }

        int halfIndex = (int) Math.ceil((double) stationsOnRoute.size() / 2.0);
        int firstIndex = random.nextInt(halfIndex);
        int secondIndex = firstIndex + random.nextInt(stationsOnRoute.size() - firstIndex - 1) + 1;

        return Optional.of(Siblings.of(stationsOnRoute.get(firstIndex),
                stationsOnRoute.get(secondIndex)));
    }

    /**
     * @return If busAgent finished execution
     */
    public boolean runBasedOnTimetable() {
        var state = this.getAgentState().getValue();
        if (state != AgentState.cAGENT_STATE_INITIATED) {
            return state == AgentState.cAGENT_STATE_ACTIVE && bus.isAtDestination();
        }

        if (shouldStart()) {
            start();
        }

        return false;
    }

    public boolean shouldStart() {
        return bus.shouldStart();
    }


}
