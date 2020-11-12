package agents;

import agents.abstractions.AbstractAgent;
import agents.utilities.MessageParameter;
import com.google.common.eventbus.EventBus;
import events.web.vehicle.VehicleAgentRouteChangedEvent;
import events.web.vehicle.VehicleAgentUpdatedEvent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ThreadedBehaviourFactory;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Properties;
import osmproxy.ExtendedGraphHopper;
import routing.abstractions.IRouteGenerator;
import routing.abstractions.IRouteTransformer;
import routing.core.IGeoPosition;
import routing.core.Position;
import routing.data.RouteMergeInfo;
import routing.nodes.LightManagerNode;
import routing.nodes.RouteNode;
import smartcity.ITimeProvider;
import smartcity.SmartCityAgent;
import smartcity.config.abstractions.ITroublePointsConfigContainer;
import vehicles.MovingObject;
import vehicles.enums.DrivingState;
import jade.core.behaviours.OneShotBehaviour;

import java.util.*;

import static agents.message.MessageManager.createMessage;
import static agents.message.MessageManager.createProperties;
import static routing.RoutingConstants.STEP_CONSTANT;

@SuppressWarnings("serial")
public class VehicleAgent extends AbstractAgent {
    private static final Random random = new Random();
    private static final int THRESHOLD_UNTIL_INDEX_CHANGE = 50;
    protected static final int NO_CONSTRUCTION_SITE_STRATEGY_FACTOR = 20;

    private final MovingObject vehicle;
    private final IRouteGenerator routeGenerator;
    private final IRouteTransformer routeTransformer;
    private final ITroublePointsConfigContainer configContainer;
    private RouteNode troublePoint;
    private final Set<Integer> trafficJamsEdgeId = new HashSet<>();
    private final Set<Long> constructionsEdgeId = new HashSet<>();

    VehicleAgent(int id, MovingObject vehicle,
                 ITimeProvider timeProvider,
                 IRouteGenerator routeGenerator,
                 IRouteTransformer routeTransformer,
                 EventBus eventBus,
                 ITroublePointsConfigContainer configContainer) {
        super(id, vehicle.getVehicleType(), timeProvider, eventBus);
        this.vehicle = vehicle;
        this.routeGenerator = routeGenerator;
        this.routeTransformer = routeTransformer;
        this.configContainer = configContainer;
    }

    @Override
    protected void setup() {
        informLightManager(vehicle);
        vehicle.setState(DrivingState.MOVING);
        int speed = vehicle.getSpeed();
        if (speed > STEP_CONSTANT) {
            print("Invalid speed: " + speed + "\n   Terminating!!!   \n");
            doDelete();
            return;
        }

        Behaviour move = new TickerBehaviour(this, STEP_CONSTANT / speed) {
            @Override
            public void onTick() {
                if (vehicle.isAtTrafficLights()) {
                    switch (vehicle.getState()) {
                        case MOVING:
                            vehicle.setState(DrivingState.WAITING_AT_LIGHT);
                            LightManagerNode light = vehicle.getCurrentTrafficLightNode();
                            ACLMessage msg = createMessageById(ACLMessage.REQUEST_WHEN, LightManagerAgent.name,
                                    light.getLightManagerId());
                            Properties properties = createProperties(MessageParameter.VEHICLE);
                            properties.setProperty(MessageParameter.ADJACENT_OSM_WAY_ID, Long.toString(vehicle.getAdjacentOsmWayId()));
                            msg.setAllUserDefinedParameters(properties);
                            send(msg);
                            logger.debug("Ask LightManager" + light.getLightManagerId() + " for right to passage.");
                            break;
                        case WAITING_AT_LIGHT:
                            break;
                        case PASSING_LIGHT:
                            logger.debug("Pass the traffic light");
                            move();
                            vehicle.setState(DrivingState.MOVING);
                            break;
                    }
                }
                else if (vehicle.isAtDestination()) {
                    vehicle.setState(DrivingState.AT_DESTINATION);
                    logger.debug("Reach destination");

                    ACLMessage msg = createMessage(ACLMessage.INFORM, SmartCityAgent.name);
                    var prop = createProperties(MessageParameter.VEHICLE);
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
            @Override
            public void action() {
                ACLMessage rcv = receive();
                if (rcv != null) {
                    switch (rcv.getPerformative()) {
                        case ACLMessage.REQUEST -> {
                            ACLMessage response = createMessage(ACLMessage.AGREE, rcv.getSender());
                            Properties properties = createProperties(MessageParameter.VEHICLE);
                            properties.setProperty(MessageParameter.ADJACENT_OSM_WAY_ID,
                                    Long.toString(vehicle.getAdjacentOsmWayId()));
                            response.setAllUserDefinedParameters(properties);
                            send(response);

                            informLightManager(vehicle);
                            vehicle.setState(DrivingState.PASSING_LIGHT);
                        }
                        case ACLMessage.AGREE -> vehicle.setState(DrivingState.WAITING_AT_LIGHT);
                        case ACLMessage.PROPOSE -> {
                            if (rcv.getUserDefinedParameter(MessageParameter.TYPEOFTROUBLE).equals(MessageParameter.CONSTRUCTION)) {
                                logger.debug("Handle construction jam");
                                handleConstructionJam(rcv);
                            }
                            else if (rcv.getUserDefinedParameter(MessageParameter.TYPEOFTROUBLE).equals(MessageParameter.TRAFFIC_JAM)) {
                                if (rcv.getSender().getLocalName().equals(TroubleManagerAgent.name)) {
                                    logger.debug("Handle traffic jams from trouble manager");
                                    handleTrafficJamsFromTroubleManager(rcv);
                                }
                                else {
                                    logger.debug("Handle traffic jams from light manager");
                                    handleTrafficJamsFromLightManager(rcv, MessageParameter.SHOW);
                                }
                            }
                        }
                        case ACLMessage.CANCEL -> {
                            if (rcv.getUserDefinedParameter(MessageParameter.TYPEOFTROUBLE).equals(MessageParameter.TRAFFIC_JAM)) {
                                logger.debug("Handle traffic jam stop from light manager");
                                handleTrafficJamsFromLightManager(rcv, MessageParameter.STOP);
                            }
                        }
                    }
                }
                block(100);
            }

            private void handleConstructionJam(ACLMessage rcv) {
                Long edgeId = Long.parseLong(rcv.getUserDefinedParameter(MessageParameter.EDGE_ID));
                logger.debug("Got propose to change the route and exclude: " + edgeId);
                if (constructionsEdgeId.contains(edgeId)) {
                    logger.debug("Already notified about construction place on edge: " + edgeId);
                    return;
                }
                constructionsEdgeId.add(edgeId);
                final Integer indexOfRouteNodeWithEdge = vehicle.findIndexOfEdgeOnRoute(edgeId,
                        THRESHOLD_UNTIL_INDEX_CHANGE);
                if (indexOfRouteNodeWithEdge != null && indexOfRouteNodeWithEdge != vehicle.getUniformRouteSize() - 1) {
                    handleConstructionSiteRouteChange(indexOfRouteNodeWithEdge);
                }
            }

            private void handleConstructionSiteRouteChange(final int indexOfRouteNodeWithEdge) {
                int indexAfterWhichRouteChanges;
                if (configContainer.shouldChangeRouteOnTroublePoint()) {
                    if (indexOfRouteNodeWithEdge - vehicle.getMoveIndex() > THRESHOLD_UNTIL_INDEX_CHANGE){
                        indexAfterWhichRouteChanges = vehicle.getFarOnIndex(THRESHOLD_UNTIL_INDEX_CHANGE);
                    } else {
                        indexAfterWhichRouteChanges = vehicle.getMoveIndex();
                    }
                } else {
                    indexAfterWhichRouteChanges = Math.max(indexOfRouteNodeWithEdge -
                            (NO_CONSTRUCTION_SITE_STRATEGY_FACTOR * THRESHOLD_UNTIL_INDEX_CHANGE), 0);
                }
                
                ThreadedBehaviourFactory factory = new ThreadedBehaviourFactory();
				Behaviour mergeUpdateBehaviour = new OneShotBehaviour() {

					@Override
					public void action() {
						final RouteMergeInfo mergeResult = createMergedWithOldRouteAlternativeRouteFromIndex(
								indexAfterWhichRouteChanges, true);
						updateVehicleRouteAfterMerge(indexAfterWhichRouteChanges, mergeResult);
					}

				};
				addBehaviour(factory.wrap(mergeUpdateBehaviour));
            }

            private RouteMergeInfo createMergedWithOldRouteAlternativeRouteFromIndex(final int indexAfterWhichRouteChanges,
                                                                                     boolean bewareOfJammedEdge) {
                final IGeoPosition positionAfterWhichRouteChanges = vehicle
                        .getPositionOnIndex(indexAfterWhichRouteChanges);
                var oldUniformRoute = vehicle.getUniformRoute();
                var newSimpleRouteEnd = routeGenerator.generateRouteInfo(positionAfterWhichRouteChanges,
                        oldUniformRoute.get(oldUniformRoute.size() - 1),
                        bewareOfJammedEdge);
                var newRouteAfterChangeIndex = routeTransformer.uniformRoute(newSimpleRouteEnd);
                var route = oldUniformRoute.subList(0, indexAfterWhichRouteChanges);
                route.addAll(newRouteAfterChangeIndex);
                final RouteMergeInfo mergeResult = routeTransformer.mergeByDistance(vehicle.getSimpleRoute(),
                        newSimpleRouteEnd);
                mergeResult.newUniformRoute = route;

                logger.debug("OLD & NEW"+ oldUniformRoute.size() + "  "+ mergeResult.newUniformRoute.size() );

                return mergeResult;
            }

            private void updateVehicleRouteAfterMerge(final int indexAfterWhichRouteChanges,
                                                      final RouteMergeInfo mergeResult) {
                final IGeoPosition positionAfterWhichRouteChanges = vehicle
                        .getPositionOnIndex(indexAfterWhichRouteChanges);
                if (!vehicle.currentTrafficLightNodeWithinAlternativeRouteThreshold(indexAfterWhichRouteChanges)) {
                    sendRefusalMessageToLightManagerAfterRouteChange();
                }
                vehicle.setRoutes(mergeResult.mergedRoute, mergeResult.newUniformRoute);
                vehicle.switchToNextTrafficLight();
                eventBus.post(new VehicleAgentRouteChangedEvent(getId(), mergeResult.startNodes,
                        positionAfterWhichRouteChanges, mergeResult.newSimpleRouteEnd));
            }

            private void handleTrafficJamsFromLightManager(ACLMessage rcv, String showOrStop) {
                int currentInternalID = vehicle.getRouteNodeBeforeLight().getInternalEdgeId();
                logger.debug("Internal edge ID when light manager asked: " + currentInternalID);
                Position positionOfTroubleLight = Position.of(rcv.getUserDefinedParameter(MessageParameter.TROUBLE_LAT),
                        rcv.getUserDefinedParameter(MessageParameter.TROUBLE_LON));

                sendMessageAboutTrafficJamTrouble(currentInternalID, positionOfTroubleLight,
                        rcv.getAllUserDefinedParameters().containsKey(MessageParameter.LENGTH_OF_JAM) ? Double.parseDouble(
                                rcv.getUserDefinedParameter(MessageParameter.LENGTH_OF_JAM)) : null,
                        showOrStop, TroubleManagerAgent.name, ACLMessage.INFORM,
                        rcv.getUserDefinedParameter(MessageParameter.ADJACENT_OSM_WAY_ID));


                sendMessageAboutTrafficJamTrouble(currentInternalID, positionOfTroubleLight,
                        rcv.getAllUserDefinedParameters().containsKey(MessageParameter.LENGTH_OF_JAM) ? Double.parseDouble(
                                rcv.getUserDefinedParameter(MessageParameter.LENGTH_OF_JAM)) : null,
                        showOrStop, rcv.getSender().getLocalName(), ACLMessage.CONFIRM,
                        rcv.getUserDefinedParameter(MessageParameter.ADJACENT_OSM_WAY_ID));
            }

            private void sendMessageAboutTrafficJamTrouble(int currentInternalID, Position positionOfTroubleLight,
                                                           Double lengthOfJam, String showOrStop, String name, int performative,
                                                           String adjOsmWayId) {
                ACLMessage msg = createMessage(performative, name);
                Properties properties = createProperties(MessageParameter.VEHICLE);
                properties.setProperty(MessageParameter.TYPEOFTROUBLE, MessageParameter.TRAFFIC_JAM);
                properties.setProperty(MessageParameter.TROUBLE, showOrStop);
                properties.setProperty(MessageParameter.ADJACENT_OSM_WAY_ID, adjOsmWayId);
                properties.setProperty(MessageParameter.TROUBLE_LAT, String.valueOf(positionOfTroubleLight.getLat()));
                properties.setProperty(MessageParameter.TROUBLE_LON, String.valueOf(positionOfTroubleLight.getLng()));
                if (lengthOfJam != null) {
                    properties.setProperty(MessageParameter.LENGTH_OF_JAM, String.valueOf(lengthOfJam));
                }
                properties.setProperty(MessageParameter.EDGE_ID, Long.toString(currentInternalID));
                msg.setAllUserDefinedParameters(properties);
                logger.debug("Send message about trouble on edge " + Long.toString(currentInternalID) + " with position: " +
                        positionOfTroubleLight.toString());
                send(msg);
            }

            private void handleTrafficJamsFromTroubleManager(ACLMessage rcv) {
                if (vehicle.isAtTrafficLights()) {
                    logger.debug("Already on the light. Can't change route");
                    return;
                }
                int edgeId = Integer.parseInt(rcv.getUserDefinedParameter(MessageParameter.EDGE_ID));
                String showOrStop = rcv.getUserDefinedParameter(MessageParameter.TROUBLE);
                boolean jamStart;
                if (showOrStop.equals(MessageParameter.SHOW)) {
                    if (trafficJamsEdgeId.contains(edgeId)) {
                        logger.debug("Already notified about traffic jam on edge: " + edgeId);
                        return;
                    }
                    trafficJamsEdgeId.add(edgeId);
                    jamStart = true;
                }
                else {
                    logger.debug("Traffic jam end on edge: " + edgeId);
                    trafficJamsEdgeId.remove(edgeId);
                    jamStart = false;
                }
                double howLongTakesJam = 0;
                if (rcv.getAllUserDefinedParameters().containsKey(MessageParameter.LENGTH_OF_JAM)) {
                    howLongTakesJam = 1000 * Double.parseDouble(rcv.getUserDefinedParameter(MessageParameter.LENGTH_OF_JAM));
                }
                double timeForTheEndWithoutJam = vehicle.getMillisecondsFromAToB(vehicle.getMoveIndex(),
                        vehicle.getUniformRoute().size() - 1);
                double timeForTheEndWithJam = timeForTheEndWithoutJam + howLongTakesJam;
                final Integer indexOfRouteNodeWithEdge = vehicle.findIndexOfEdgeOnRoute((long) edgeId,
                        THRESHOLD_UNTIL_INDEX_CHANGE);
                int indexAfterWhichRouteChanges;
                if (indexOfRouteNodeWithEdge != null || !jamStart) {
                    indexAfterWhichRouteChanges = vehicle.getFarOnIndex(THRESHOLD_UNTIL_INDEX_CHANGE);
                    if (indexAfterWhichRouteChanges == vehicle.getUniformRouteSize() - 1) {
                        return;
                    }
                    handleLightTrafficJamRouteChange(indexAfterWhichRouteChanges, timeForTheEndWithJam, jamStart);
                }
            }

            private void handleLightTrafficJamRouteChange(final int indexAfterWhichRouteChanges,
                                                          final double timeForTheEndWithJam, boolean bewareOfJammedEdge) {
                logger.info("Jammed traffic light on route, handle it");
                double timeForOfDynamicRoute = 0;
                final RouteMergeInfo mergeResult = createMergedWithOldRouteAlternativeRouteFromIndex(indexAfterWhichRouteChanges,
                        bewareOfJammedEdge);
                timeForOfDynamicRoute = vehicle.getMillisecondsFromAToB(vehicle.getMoveIndex(),
                        mergeResult.newUniformRoute.size() - 1);
                //logger.info("=======================timeForOfDynamicRoute" + timeForOfDynamicRoute);
                //logger.info("-----------------------timeForTheEndWithJam" + timeForTheEndWithJam);
                if (timeForTheEndWithJam > timeForOfDynamicRoute) {
                    logger.info("Trip time through the jam: " + timeForTheEndWithJam + " is longer than alternative route time: "
                            + timeForOfDynamicRoute + ", so route will be changed");
                    // TODO: CHECK IF send refusal is on place // switchToNextLight was after this line
                    updateVehicleRouteAfterMerge(indexAfterWhichRouteChanges, mergeResult);
                }
                else {
                    logger.info("Trip time through the jam: " + timeForTheEndWithJam + " is shorter than alternative route time: "
                            + timeForOfDynamicRoute + ", so route won't be changed");
                }
            }

            private void sendRefusalMessageToLightManagerAfterRouteChange() {
                LightManagerNode currentManager = vehicle.getCurrentTrafficLightNode(); //change route, that is why send stop
                if (currentManager != null) {
                    ACLMessage msg = createMessage(ACLMessage.REFUSE, LightManagerAgent.name +
                            currentManager.getLightManagerId());
                    Properties properties = createProperties(MessageParameter.VEHICLE);
                    properties.setProperty(MessageParameter.ADJACENT_OSM_WAY_ID, Long.toString(currentManager.getAdjacentWayId()));
                    send(msg);
                    print("Send refuse to LightManager" + currentManager.getLightManagerId());
                }
            }
        };

        addBehaviour(move);
        addBehaviour(communication);

        if (configContainer.shouldGenerateConstructionSites()) {
            var timeBeforeTroubleMs = this.configContainer.getTimeBeforeTrouble() * 1000;
            Behaviour troubleGenerator = new TickerBehaviour(this, timeBeforeTroubleMs) {

                @Override
                public void onTick() {
                    var route = vehicle.getUniformRoute();

                    var el = random.nextInt(route.size()  - vehicle.getMoveIndex() + 1)+ vehicle.getMoveIndex() ; // TODO: from current index //choose trouble EdgeId
                    RouteNode troublePointTmp = route.get(el);
                    troublePoint = new RouteNode(troublePointTmp.getLat(), troublePointTmp.getLng(),
                            troublePointTmp.getInternalEdgeId());
                    sendMessageAboutConstructionTrouble(); //send message to boss Agent
                    stop();
                }

                private void sendMessageAboutConstructionTrouble() {
                    ACLMessage msg = createMessage(ACLMessage.INFORM, TroubleManagerAgent.name);
                    Properties properties = createProperties(MessageParameter.VEHICLE);
                    properties.setProperty(MessageParameter.TYPEOFTROUBLE, MessageParameter.CONSTRUCTION);
                    properties.setProperty(MessageParameter.TROUBLE, MessageParameter.SHOW);
                    properties.setProperty(MessageParameter.TROUBLE_LAT, Double.toString(troublePoint.getLat()));
                    properties.setProperty(MessageParameter.TROUBLE_LON, Double.toString(troublePoint.getLng()));

                    properties.setProperty(MessageParameter.EDGE_ID, Long.toString(troublePoint.getInternalEdgeId()));
                    msg.setAllUserDefinedParameters(properties);
                    logger.debug("Send message about construction site on edge " + Long.toString(troublePoint.getInternalEdgeId()));
                    send(msg);
                }

            };


            Behaviour troubleStopper = new TickerBehaviour(this, 3 * timeBeforeTroubleMs) {

                @Override
                public void onTick() {
                    sendMessageAboutTroubleStop(MessageParameter.CONSTRUCTION);
                    ExtendedGraphHopper.removeForbiddenEdges(Arrays.asList(troublePoint.getInternalEdgeId()));
                    stop();
                }

                private void sendMessageAboutTroubleStop(String type) {
                    ACLMessage msg = createMessage(ACLMessage.INFORM, TroubleManagerAgent.name);
                    Properties properties = createProperties(MessageParameter.VEHICLE);
                    properties.setProperty(MessageParameter.TROUBLE, MessageParameter.STOP);
                    properties.setProperty(MessageParameter.TYPEOFTROUBLE, type);
                    properties.setProperty(MessageParameter.TROUBLE_LAT, Double.toString(troublePoint.getLat()));
                    properties.setProperty(MessageParameter.TROUBLE_LON, Double.toString(troublePoint.getLng()));
                    properties.setProperty(MessageParameter.EDGE_ID, Long.toString(troublePoint.getInternalEdgeId()));
                    msg.setAllUserDefinedParameters(properties);
                    print("Send message about trouble stop on edge " + Long.toString(troublePoint.getInternalEdgeId()));
                    send(msg);
                }
            };
            addBehaviour(troubleGenerator);
            addBehaviour(troubleStopper);
        }
    }


    private void displayRouteDebug(List<RouteNode> route) {
        for (RouteNode node : route) {
            System.out.print(node.getDebugString(node instanceof LightManagerNode));
            System.out.println();
        }
    }

    public MovingObject getVehicle() {
        return vehicle;
    }

    public void move() {
        vehicle.move();
        eventBus.post(new VehicleAgentUpdatedEvent(this.getId(), vehicle.getPosition()));
    }

    public IGeoPosition getPosition() {
        return vehicle.getPosition();
    }
}
