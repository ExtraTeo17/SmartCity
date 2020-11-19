package agents;

import agents.abstractions.AbstractAgent;
import agents.utilities.MessageParameter;
import com.google.common.eventbus.EventBus;
import events.web.pedestrian.PedestrianAgentEnteredBusEvent;
import events.web.pedestrian.PedestrianAgentLeftBusEvent;
import events.web.pedestrian.PedestrianAgentUpdatedEvent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Properties;
import osmproxy.HighwayAccessor;
import routing.RoutingConstants;
import routing.abstractions.IRouteGenerator;
import routing.core.IGeoPosition;
import routing.core.Position;
import routing.nodes.LightManagerNode;
import routing.nodes.RouteNode;
import routing.nodes.StationNode;
import smartcity.ITimeProvider;
import smartcity.SmartCityAgent;
import vehicles.Pedestrian;
import vehicles.TestPedestrian;
import vehicles.enums.DrivingState;

import static agents.message.MessageManager.createMessage;
import static agents.message.MessageManager.createProperties;

import java.time.LocalTime;
import java.util.List;

public class PedestrianAgent extends AbstractAgent {
    public static final String name = PedestrianAgent.class.getSimpleName().replace("Agent", "");

    private  Pedestrian pedestrian;
    private final IRouteGenerator router;
    private List<RouteNode> arrivingRouteToClosestStation = null;
    private List<RouteNode> bikeRoute = null;


    PedestrianAgent(int agentId,
                    Pedestrian pedestrian,
                    ITimeProvider timeProvider,
                    EventBus eventBus,
                    IRouteGenerator router) {
        super(agentId, pedestrian.getVehicleType(), timeProvider, eventBus);
        this.pedestrian = pedestrian;
        this.router = router;
    }

    public boolean isInBus() { return DrivingState.IN_BUS == pedestrian.getState(); }

    @Override
    protected void setup() {
        getNextStation();
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
                            properties.setProperty(MessageParameter.ADJACENT_OSM_WAY_ID, Long.toString(pedestrian.getAdjacentOsmWayId()));
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
                }
                else if (pedestrian.isAtStation()) {
                    switch (pedestrian.getState()) {
                        case MOVING:
                            StationNode station = pedestrian.getStartingStation();
                            ACLMessage msg = createMessageById(ACLMessage.REQUEST_WHEN, StationAgent.name, station.getAgentId());
                            Properties properties = createProperties(MessageParameter.PEDESTRIAN);
                            properties.setProperty(MessageParameter.DESIRED_OSM_STATION_ID, pedestrian.getTargetStation().getOsmId()+"");
                            properties.setProperty(MessageParameter.ARRIVAL_TIME, timeProvider.getCurrentSimulationTime()
                                    .toString());
                            msg.setAllUserDefinedParameters(properties);
                            send(msg);
                           // print("Send REQUEST_WHEN to Station");

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
                }
                else if (pedestrian.isAtDestination()) {
                    pedestrian.setState(DrivingState.AT_DESTINATION);
                    print("Reached destination.");

                    ACLMessage msg = createMessage(ACLMessage.INFORM, SmartCityAgent.name);
                    Properties prop = createProperties(MessageParameter.PEDESTRIAN);
                    prop.setProperty(MessageParameter.AT_DESTINATION, String.valueOf(Boolean.TRUE));
                    msg.setAllUserDefinedParameters(prop);
                    send(msg);
                    doDelete();
                } else if (!pedestrian.isTroubled()) {
                	move();
                }
            }
        };

        Behaviour communication = new CyclicBehaviour() {
        	
            private long bikeTime;
            private StationNode expectedNewStationNode;
            private IGeoPosition  currentPosition;
			@Override
            public void action() {
                ACLMessage rcv = receive();
                if (rcv == null) {
                    return;
                }

                String type = rcv.getUserDefinedParameter(MessageParameter.TYPE);
                if (type == null) {
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
                            properties.setProperty(MessageParameter.DESIRED_OSM_STATION_ID, pedestrian.getTargetStation().getOsmId()+"");
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
                            }
                            informLightManager(pedestrian);
                        } else if (rcv.getPerformative() == ACLMessage.INFORM) {
                        	pedestrian.setTroubled(true);
                            logger.info("Get info about trouble from bus");

                             expectedNewStationNode  = new StationNode(	rcv.getUserDefinedParameter(MessageParameter.LAT_OF_NEXT_CLOSEST_STATION),
                                    rcv.getUserDefinedParameter(MessageParameter.LON_OF_NEXT_CLOSEST_STATION),
                                    rcv.getUserDefinedParameter(MessageParameter.DESIRED_OSM_STATION_ID),
                                    rcv.getUserDefinedParameter(MessageParameter.AGENT_ID_OF_NEXT_CLOSEST_STATION));


                             currentPosition = Position.of(rcv.getUserDefinedParameter(MessageParameter.TROUBLE_LAT),
                            		rcv.getUserDefinedParameter(MessageParameter.TROUBLE_LON));

                            IGeoPosition nextClosestStationPosition = Position.of(expectedNewStationNode.getLat()+"",
                                    expectedNewStationNode.getLng()+"");

                            LocalTime arrivalTime = computeArrivalTime(currentPosition, nextClosestStationPosition,
                                    expectedNewStationNode.getOsmId()+"");

                            ACLMessage messageToBusManager = createMessage(ACLMessage.INFORM, BusManagerAgent.NAME);

                            messageToBusManager.addUserDefinedParameter(MessageParameter.ARRIVAL_TIME, arrivalTime.toString());

                            messageToBusManager.addUserDefinedParameter(MessageParameter.STATION_FROM_ID,
                                    expectedNewStationNode.getOsmId()+"");

                            messageToBusManager.addUserDefinedParameter(MessageParameter.STATION_TO_ID,
                            		pedestrian.getTargetStation().getOsmId() + "");

                            send(messageToBusManager);
                            computeBikeTime(currentPosition, pedestrian.getUniformRoute()
                            		.get(pedestrian.getUniformRouteSize() - 1));
                            //decideWhereToGo(rcv);
                        }
                        break;
                    case MessageParameter.BUS_MANAGER:
                    	if (rcv.getPerformative() == ACLMessage.INFORM) {
                    	    logger.info("Got Inform message from BUS MANAGER");
                    		long timeBetweenArrivalAtStationAndDesiredStation = Long.parseLong(rcv.getUserDefinedParameter(
                    				MessageParameter.TIME_BETWEEN_PEDESTRIAN_AT_STATION_ARRIVAL_AND_REACHING_DESIRED_STOP));
                    		String newBusLine = rcv.getUserDefinedParameter(MessageParameter.BUS_LINE);
                    		long busTime = (pedestrian.getMillisecondsOnRoute(arrivingRouteToClosestStation) * 1_000) +
                    				timeBetweenArrivalAtStationAndDesiredStation +
                    				(pedestrian.getMillisecondsOnRoute(pedestrian.getUniformRoute()) * 1_000);
                            logger.info("BikeTime:"+ bikeTime +"vs bustime: " + busTime);
                    		if (bikeTime > busTime) {
                    			restartAgentWithNewBusLine(arrivingRouteToClosestStation);
                    		} else {
                                logger.info("Chosen bike because bikeTime:"+ bikeTime +"vs bustime: " + busTime);
                    			performMetamorphosisToBike();
                    		}
                    	}
                }
            }

            private void performMetamorphosisToBike() {
			  pedestrian.getTaskProvider().getCreateBikeTask(currentPosition, pedestrian.getEndPosition(),   pedestrian instanceof TestPedestrian).run();
			  myAgent.doDelete();
			  logger.info("Kill Pedestrian Agent");
			  }

			private void restartAgentWithNewBusLine(List<RouteNode> arrivingRouteToClosestStation) {
			    pedestrian = new Pedestrian(pedestrian.getAgentId(),
                        arrivingRouteToClosestStation,
                        arrivingRouteToClosestStation,
                pedestrian.getDisplayRouteAfterBus(),
                        pedestrian.getDisplayRouteAfterBus(),
                        expectedNewStationNode,
                pedestrian.getStationFinish(),
                pedestrian.getTimeProvider(),
                pedestrian.getTaskProvider());
                getNextStation();
                informLightManager(pedestrian);

                pedestrian.setState(DrivingState.MOVING);

			}

			private void computeBikeTime(IGeoPosition pointA, IGeoPosition pointB) {
				bikeRoute = router.generateRouteInfo(pointA, pointB, "bike");
				int firstIndex = 0, bikeSpeed = 20;
			    bikeTime = pedestrian.getMillisecondsOnRoute(bikeRoute, firstIndex, bikeSpeed);
			}

			private LocalTime computeArrivalTime(IGeoPosition pointA, IGeoPosition pointB, String desiredOsmStationId) {
				LocalTime now = timeProvider.getCurrentSimulationTime().toLocalTime();
				arrivingRouteToClosestStation = router.generateRouteForPedestrians(pointA, pointB, null, desiredOsmStationId);
				LocalTime arrivingTime = now.plusNanos(pedestrian.getMillisecondsOnRoute(arrivingRouteToClosestStation) * 1_000_000);
				return arrivingTime;
			}




            private StationNode getStationNodeFromMessage(ACLMessage rcv) {
                return new StationNode(rcv.getUserDefinedParameter(MessageParameter.LAT_OF_NEXT_CLOSEST_STATION),
                                rcv.getUserDefinedParameter(MessageParameter.LON_OF_NEXT_CLOSEST_STATION),
                                rcv.getUserDefinedParameter(MessageParameter.OSM_ID_OF_NEXT_CLOSEST_STATION),
                                rcv.getUserDefinedParameter(MessageParameter.AGENT_ID_OF_NEXT_CLOSEST_STATION));
            }
        };

        addBehaviour(move);
        addBehaviour(communication);
    }

    private void getNextStation() {
        // finds next station and announces his arrival
        StationNode nextStation = pedestrian.findNextStation();
        pedestrian.setState(DrivingState.MOVING);
        if (nextStation != null) {
            ACLMessage msg = createMessageById(ACLMessage.INFORM, StationAgent.name, nextStation.getAgentId());
            Properties properties = createProperties(MessageParameter.PEDESTRIAN);
            var currentTime = timeProvider.getCurrentSimulationTime();
            var predictedTime = currentTime.plusNanos(pedestrian.getMillisecondsToNextStation() * 1_000_000);
            properties.setProperty(MessageParameter.ARRIVAL_TIME, predictedTime.toString());
            properties.setProperty(MessageParameter.DESIRED_OSM_STATION_ID, pedestrian.getTargetStation().getOsmId()+"");
            msg.setAllUserDefinedParameters(properties);

            send(msg);
            print("Sent INFORM to Station");
        }
    }

    private void move() {
        pedestrian.move();
        eventBus.post(new PedestrianAgentUpdatedEvent(this.getId(), pedestrian.getPosition()));
    }

    private void enterBus() {
        pedestrian.setState(DrivingState.IN_BUS);
        eventBus.post(new PedestrianAgentEnteredBusEvent(this.getId()));
    }

    private void quitBus() {
        pedestrian.move();
        pedestrian.setState(DrivingState.PASSING_STATION);
        eventBus.post(new PedestrianAgentLeftBusEvent(this.getId(), pedestrian.getPosition()));
    }

    public IGeoPosition getPosition() {
        return pedestrian.getPosition();
    }

    public Pedestrian getPedestrian() {
        return pedestrian;
    }
}
