package agents;

import agents.abstractions.AbstractAgent;
import agents.utilities.MessageParameter;
import com.google.common.eventbus.EventBus;
import events.web.car.CarAgentRouteChangedEvent;
import events.web.car.CarAgentUpdatedEvent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Properties;
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

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static agents.message.MessageManager.createMessage;
import static agents.message.MessageManager.createProperties;
import static routing.RoutingConstants.STEP_CONSTANT;

@SuppressWarnings("serial")
public class CarAgent extends AbstractAgent {
    private static final Random random = new Random();
    private static final int THRESHOLD_UNTIL_INDEX_CHANGE = 50;
    protected static final int NO_CONSTRUCTION_SITE_STRATEGY_FACTOR = 20;

    private final MovingObject car;
    private final IRouteGenerator routeGenerator;
    private final IRouteTransformer routeTransformer;
    private final ITroublePointsConfigContainer configContainer;
    private final Set<Integer> trafficJamsEdgeId = new HashSet<>();
    private final Set<Long> constructionsEdgeId = new HashSet<>();

    private RouteNode troublePoint;
    private Integer borderlineIndex = null;

    CarAgent(int id, MovingObject car,
             ITimeProvider timeProvider,
             IRouteGenerator routeGenerator,
             IRouteTransformer routeTransformer,
             EventBus eventBus,
             ITroublePointsConfigContainer configContainer) {
        super(id, car.getVehicleType(), timeProvider, eventBus);
        this.car = car;
        this.routeGenerator = routeGenerator;
        this.routeTransformer = routeTransformer;
        this.configContainer = configContainer;
    }

    @Override
    protected void setup() {
        informLightManager(car);
        car.setState(DrivingState.MOVING);
        int speed = car.getSpeed();
        if (speed > STEP_CONSTANT) {
            print("Invalid speed: " + speed + "\n   Terminating!!!   \n");
            doDelete();
            return;
        }

        Behaviour move = new TickerBehaviour(this, STEP_CONSTANT / speed) {
            @Override
            public void onTick() {
                if (car.isAtTrafficLights()) {
                    switch (car.getState()) {
                        case MOVING:
                            LightManagerNode light = car.getCurrentTrafficLightNode();
                            if (light == null) {
                                move();
                                return;
                            }
                            car.setState(DrivingState.WAITING_AT_LIGHT);
                            ACLMessage msg = createMessageById(ACLMessage.REQUEST_WHEN, LightManagerAgent.name,
                                    light.getLightManagerId());
                            Properties properties = createProperties(MessageParameter.VEHICLE);
                            properties.setProperty(MessageParameter.ADJACENT_OSM_WAY_ID, Long.toString(car.getAdjacentOsmWayId()));
                            msg.setAllUserDefinedParameters(properties);
                            send(msg);
                            logger.debug("Ask LightManager" + light.getLightManagerId() + " for right to passage.");
                            break;
                        case WAITING_AT_LIGHT:
                            break;
                        case PASSING_LIGHT:
                            logger.debug("Pass the traffic light");
                            move();
                            car.setState(DrivingState.MOVING);
                            break;
                    }
                }
                else if (car.isAtDestination()) {
                    car.setState(DrivingState.AT_DESTINATION);
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
                                    Long.toString(car.getAdjacentOsmWayId()));
                            response.setAllUserDefinedParameters(properties);
                            send(response);

                            informLightManager(car);
                            car.setState(DrivingState.PASSING_LIGHT);
                        }
                        case ACLMessage.AGREE -> car.setState(DrivingState.WAITING_AT_LIGHT);
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
                logger.info("Got propose to change the route and exclude: " + edgeId);
                if (constructionsEdgeId.contains(edgeId)) {
                    logger.info("Already notified about construction place on edge: " + edgeId);
                    return;
                }
                constructionsEdgeId.add(edgeId);
                final Integer indexOfRouteNodeWithEdge = car.findIndexOfEdgeOnRoute(edgeId,
                        THRESHOLD_UNTIL_INDEX_CHANGE);


                if (indexOfRouteNodeWithEdge != null && indexOfRouteNodeWithEdge != car.getUniformRouteSize() - 1) {
                    handleConstructionSiteRouteChange(indexOfRouteNodeWithEdge);
                }
                else {
                    logger.info("Index of edge route is invalid: " + indexOfRouteNodeWithEdge);
                }
            }

            private void handleConstructionSiteRouteChange(final int indexOfRouteNodeWithEdge) {
                int indexAfterWhichRouteChanges;
                if (configContainer.shouldChangeRouteOnTroublePoint()) {
                    if (indexOfRouteNodeWithEdge - car.getMoveIndex() > THRESHOLD_UNTIL_INDEX_CHANGE) {
                        indexAfterWhichRouteChanges = car.getNextNonVirtualIndex(THRESHOLD_UNTIL_INDEX_CHANGE);
                    }
                    else {
                        indexAfterWhichRouteChanges = car.getNextNonVirtualIndex();
                    }
                }
                else {
                    indexAfterWhichRouteChanges = Math.max(indexOfRouteNodeWithEdge -
                            (NO_CONSTRUCTION_SITE_STRATEGY_FACTOR * THRESHOLD_UNTIL_INDEX_CHANGE), 0);
                }

                borderlineIndex = indexAfterWhichRouteChanges;
                ThreadedBehaviourFactory factory = new ThreadedBehaviourFactory();
                Behaviour mergeUpdateBehaviour = new OneShotBehaviour() {

                    @Override
                    public void action() {
                        final RouteMergeInfo mergeResult = createMergedWithOldRouteAlternativeRouteFromIndex(
                                indexAfterWhichRouteChanges, true);
                        updateVehicleRouteAfterMerge(indexAfterWhichRouteChanges, mergeResult);
                        borderlineIndex = null;
                    }

                };
                addBehaviour(factory.wrap(mergeUpdateBehaviour));
            }

            private RouteMergeInfo createMergedWithOldRouteAlternativeRouteFromIndex(final int indexAfterWhichRouteChanges,
                                                                                     boolean bewareOfJammedEdge) {
                final IGeoPosition positionAfterWhichRouteChanges = car
                        .getPositionOnIndex(indexAfterWhichRouteChanges);
                var oldUniformRoute = car.getUniformRoute();
                var newSimpleRouteEnd = routeGenerator.generateRouteInfo(positionAfterWhichRouteChanges,
                        oldUniformRoute.get(oldUniformRoute.size() - 1),
                        bewareOfJammedEdge);
                var newRouteAfterChangeIndex = routeTransformer.uniformRoute(newSimpleRouteEnd);
                var route = oldUniformRoute.subList(0, indexAfterWhichRouteChanges);
                route.addAll(newRouteAfterChangeIndex);
                final RouteMergeInfo mergeResult = routeTransformer.mergeByDistance(car.getSimpleRoute(),
                        newSimpleRouteEnd);
                mergeResult.newUniformRoute = route;

                logger.debug("OLD & NEW" + oldUniformRoute.size() + "  " + mergeResult.newUniformRoute.size());

                return mergeResult;
            }

            private void updateVehicleRouteAfterMerge(final int indexAfterWhichRouteChanges,
                                                      final RouteMergeInfo mergeResult) {
                final IGeoPosition positionAfterWhichRouteChanges = car
                        .getPositionOnIndex(indexAfterWhichRouteChanges);
                if (!car.currentTrafficLightNodeWithinAlternativeRouteThreshold(indexAfterWhichRouteChanges)) {
                    sendRefusalMessageToLightManagerAfterRouteChange();
                }
                car.setRoutes(mergeResult.mergedRoute, mergeResult.newUniformRoute);
                car.switchToNextTrafficLight();
                eventBus.post(new CarAgentRouteChangedEvent(getId(), mergeResult.startNodes,
                        positionAfterWhichRouteChanges, mergeResult.newSimpleRouteEnd));
            }

            private void handleTrafficJamsFromLightManager(ACLMessage rcv, String showOrStop) {
                int currentInternalID = car.getRouteNodeBeforeLight().getInternalEdgeId();
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
                if (car.isAtTrafficLights()) {
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
                double timeForTheEndWithoutJam = car.getMillisecondsFromAToB(car.getMoveIndex(),
                        car.getUniformRoute().size() - 1);
                double timeForTheEndWithJam = timeForTheEndWithoutJam + howLongTakesJam;
                final Integer indexOfRouteNodeWithEdge = car.findIndexOfEdgeOnRoute((long) edgeId,
                        THRESHOLD_UNTIL_INDEX_CHANGE);
                int indexAfterWhichRouteChanges;
                if (indexOfRouteNodeWithEdge != null || !jamStart) {
                    indexAfterWhichRouteChanges = car.getFarOnIndex(THRESHOLD_UNTIL_INDEX_CHANGE);
                    if (indexAfterWhichRouteChanges == car.getUniformRouteSize() - 1) {
                        return;
                    }
                    handleLightTrafficJamRouteChange(indexAfterWhichRouteChanges, timeForTheEndWithJam, jamStart);
                }
            }

            private void handleLightTrafficJamRouteChange(final int indexAfterWhichRouteChanges,
                                                          final double timeForTheEndWithJam, boolean bewareOfJammedEdge) {
                logger.info("Jammed traffic light on route, handle it");
                double timeForOfDynamicRoute;
                final RouteMergeInfo mergeResult = createMergedWithOldRouteAlternativeRouteFromIndex(indexAfterWhichRouteChanges,
                        bewareOfJammedEdge);
                timeForOfDynamicRoute = car.getMillisecondsFromAToB(car.getMoveIndex(),
                        mergeResult.newUniformRoute.size() - 1);
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
                LightManagerNode currentManager = car.getCurrentTrafficLightNode(); //change route, that is why send stop
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
                    var route = car.getUniformRoute();

                    // TODO: from current index
                    //   choose trouble EdgeId
                    // TODO: magic numbers !!!
                    var el = random.nextInt(route.size() - car.getMoveIndex() - THRESHOLD_UNTIL_INDEX_CHANGE - 5 + 1)
                            + car.getMoveIndex() + THRESHOLD_UNTIL_INDEX_CHANGE + 5;
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

            addBehaviour(troubleGenerator);

        }
    }

    private void displayRouteDebug(List<RouteNode> route) {
        for (RouteNode node : route) {
            System.out.print(node.getDebugString(node instanceof LightManagerNode));
            System.out.println();
        }
    }

    public MovingObject getCar() {
        return car;
    }

    public void move() {
        if (borderlineIndex == null || car.getMoveIndex() < borderlineIndex) {
            car.move();
            eventBus.post(new CarAgentUpdatedEvent(this.getId(), car.getPosition()));
        }
    }

    public IGeoPosition getPosition() {
        return car.getPosition();
    }
}
