package agents;

import agents.abstractions.AbstractAgent;
import agents.utilities.MessageParameter;
import com.google.common.eventbus.EventBus;
import events.web.car.CarAgentDeadEvent;
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

import java.util.*;

import static agents.AgentConstants.DEFAULT_BLOCK_ON_ERROR;
import static agents.message.MessageManager.createMessage;
import static agents.message.MessageManager.createProperties;
import static agents.utilities.BehaviourWrapper.wrapErrors;
import static routing.RoutingConstants.STEP_CONSTANT;
import static smartcity.config.StaticConfig.USE_BATCHED_UPDATES;

/**
 * The main aim of Car agent is to get from point A to B. Car agent follows the
 * route calculated at the beginning of the simulation. As soon as it passes
 * through one light, it sends information to the next about his upcoming
 * arrival. When Car is just in front of the traffic light, he is sending a pass
 * request to {@link LightManagerAgent}. Once approved, Car drives through the
 * light and communicates with the next one until it reaches its destination.
 * Moreover, on the road can appear obstacles (e.g. construction
 * sites/accidents, traffic jams). By processing information from
 * {@link TroubleManagerAgent}, Car agent can apply chosen strategy and,
 * therefore, change the initial route.
 */
public class CarAgent extends AbstractAgent {
    private static final long CONSTRUCTION_SITE_GENERATION_SEED = 10002959;
    private static final long ID_GENERATION_SEED = 9973;

    private final MovingObject car;
    private final IRouteGenerator routeGenerator;
    private final IRouteTransformer routeTransformer;
    private final ITroublePointsConfigContainer configContainer;
    private final Set<Integer> trafficJamsEdgeId = new HashSet<>();
    private final Set<Long> constructionsEdgeId = new HashSet<>();
    private final Random random;
    private final int initialRouteSize;

    private RouteNode troublePoint;
    private Integer borderlineIndex;

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

        this.initialRouteSize = this.car.getUniformRouteSize();
        if (configContainer.shouldUseFixedConstructionSites()) {
            long seed = ID_GENERATION_SEED * id + CONSTRUCTION_SITE_GENERATION_SEED;
            this.random = new Random(seed);
        }
        else {
            this.random = new Random();
        }
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

        var onError = createErrorConsumer(new CarAgentDeadEvent(this.getId(),
                this.car.getUniformRouteSize(), null));
        var movePeriodMs = STEP_CONSTANT / speed;
        Behaviour move = new TickerBehaviour(this, movePeriodMs) {
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
                            logger.debug("Ask LightManager" + light.getLightManagerId() + " for right to passage");
                            break;
                        case WAITING_AT_LIGHT:
                            break;
                        case PASSING_LIGHT:
                            logger.info("Passed the traffic light at " + timeProvider.getCurrentSimulationTime());
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
                if (rcv == null) {
                    block();
                    return;
                }

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

                    default -> block(DEFAULT_BLOCK_ON_ERROR);
                }
            }

            private void handleConstructionJam(ACLMessage rcv) {
                Long edgeId = Long.parseLong(rcv.getUserDefinedParameter(MessageParameter.EDGE_ID));
                logger.debug("Got propose to change the route and exclude: " + edgeId);
                if (constructionsEdgeId.contains(edgeId)) {
                    logger.debug("Already notified about construction place on edge: " + edgeId);
                    return;
                }
                constructionsEdgeId.add(edgeId);
                Integer indexOfRouteNodeWithEdge = car.findIndexOfEdgeOnRoute(edgeId,
                        configContainer.getConstructionSiteThresholdUntilIndexChange());

                if (indexOfRouteNodeWithEdge != null && indexOfRouteNodeWithEdge != car.getUniformRouteSize() - 1) {
                    handleConstructionSiteRouteChange(indexOfRouteNodeWithEdge);
                }
                else {
                    logger.debug("Index of edge: " + indexOfRouteNodeWithEdge + " is not on the route");
                }
            }

            private void handleConstructionSiteRouteChange(int indexOfRouteNodeWithEdge) {
                int indexAfterWhichRouteChanges;
                if (configContainer.isConstructionSiteStrategyActive()) {
                    var threshold = configContainer.getConstructionSiteThresholdUntilIndexChange();
                    if (indexOfRouteNodeWithEdge - car.getMoveIndex() > threshold) {
                        indexAfterWhichRouteChanges = car.getNextNonVirtualIndex(threshold);
                    }
                    else {
                        indexAfterWhichRouteChanges = car.getNextNonVirtualIndex();
                    }
                }
                else {
                    var initIndex = Math.max(indexOfRouteNodeWithEdge -
                            configContainer.getNoConstructionSiteStrategyIndexFactor(), 1);
                    indexAfterWhichRouteChanges = car.getPrevNonVirtualIndexFromIndex(initIndex);
                }

                borderlineIndex = indexAfterWhichRouteChanges;
                ThreadedBehaviourFactory factory = new ThreadedBehaviourFactory();
                Behaviour mergeUpdateBehaviour = new OneShotBehaviour() {

                    @Override
                    public void action() {
                        RouteMergeInfo mergeResult = createMergedWithOldRouteAlternativeRouteFromIndex(
                                indexAfterWhichRouteChanges, true);
                        updateVehicleRouteAfterMerge(indexAfterWhichRouteChanges, mergeResult);
                        borderlineIndex = null;
                    }

                };
                addBehaviour(wrapErrors(factory.wrap(mergeUpdateBehaviour), onError));
            }


            /**
             * @param indexAfterWhichRouteChanges >= 0 && < uniformRoute.size()
             */
            private RouteMergeInfo createMergedWithOldRouteAlternativeRouteFromIndex(int indexAfterWhichRouteChanges,
                                                                                     boolean bewareOfJammedEdge) {
                var uniformRouteSize = car.getUniformRouteSize();
                if (indexAfterWhichRouteChanges >= uniformRouteSize) {
                    indexAfterWhichRouteChanges = uniformRouteSize - 1;
                }

                var positionAfterWhichRouteChanges = car.getPositionOnIndex(indexAfterWhichRouteChanges);
                var oldUniformRoute = car.getUniformRoute();

                var newSimpleRouteEnd = routeGenerator.generateRouteInfo(positionAfterWhichRouteChanges,
                        oldUniformRoute.get(oldUniformRoute.size() - 1),
                        bewareOfJammedEdge);
                if (newSimpleRouteEnd.size() == 0) { // Case when GraphHopper has problems
                    logger.warn("New route is empty. Will use old route.");
                    newSimpleRouteEnd = new ArrayList<>(oldUniformRoute.subList(indexAfterWhichRouteChanges,
                            oldUniformRoute.size()));
                }

                List<RouteNode> route = oldUniformRoute.subList(0, indexAfterWhichRouteChanges);
                route.addAll(newSimpleRouteEnd);
                RouteMergeInfo mergeResult = routeTransformer.mergeByDistance(car.getSimpleRoute(),
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
                int threshold = configContainer.getConstructionSiteThresholdUntilIndexChange();
                Integer indexOfRouteNodeWithEdge = car.findIndexOfEdgeOnRoute((long) edgeId,
                        threshold);
                int indexAfterWhichRouteChanges;
                if (indexOfRouteNodeWithEdge != null || !jamStart) {
                    indexAfterWhichRouteChanges = car.getNextNonVirtualIndex(threshold);
                    if (indexAfterWhichRouteChanges >= car.getUniformRouteSize() - 1) {
                        return;
                    }
                    handleLightTrafficJamRouteChange(indexAfterWhichRouteChanges, timeForTheEndWithJam, jamStart);
                }
            }

            private void handleLightTrafficJamRouteChange(final int indexAfterWhichRouteChanges,
                                                          final double timeForTheEndWithJam, boolean bewareOfJammedEdge) {
                logger.info("Jammed traffic light on route, handle it");
                borderlineIndex = indexAfterWhichRouteChanges;
                ThreadedBehaviourFactory factory = new ThreadedBehaviourFactory();
                Behaviour mergeUpdateBehaviour = new OneShotBehaviour() {

                    @Override
                    public void action() {
                        double timeForOfDynamicRoute;
                        final RouteMergeInfo mergeResult = createMergedWithOldRouteAlternativeRouteFromIndex(indexAfterWhichRouteChanges,
                                bewareOfJammedEdge);
                        timeForOfDynamicRoute = car.getMillisecondsFromAToB(car.getMoveIndex(),
                                mergeResult.newUniformRoute.size() - 1);

                        if (timeForTheEndWithJam > timeForOfDynamicRoute) {
                            // TODO: Check if send refusal is on place // switchToNextLight was after this line
                            updateVehicleRouteAfterMerge(indexAfterWhichRouteChanges, mergeResult);
                        }
                        borderlineIndex = null;
                    }

                };
                addBehaviour(wrapErrors(factory.wrap(mergeUpdateBehaviour), onError));
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

        addBehaviour(wrapErrors(move, onError));
        addBehaviour(wrapErrors(communication, onError));

        if (configContainer.shouldGenerateConstructionSites()) {
            var timeBeforeTroubleMs = this.configContainer.getTimeBeforeTrouble() * 1000;

            Behaviour troubleGenerator = new TickerBehaviour(this, timeBeforeTroubleMs) {
                private final int MAX_MOVE_ON_TP = timeBeforeTroubleMs / movePeriodMs;
                // TODO: From current index, choose trouble EdgeId
                private final int SAFE_BUFFER = 5;

                @Override
                public void onTick() {
                    var route = car.getUniformRoute();
                    int index = configContainer.shouldUseFixedConstructionSites() ?
                            getFixedRandomIndex() :
                            getTrulyRandomIndex(car.getMoveIndex(), route.size());

                    if (index < route.size()) {
                        RouteNode troublePointTmp = route.get(index);
                        troublePoint = new RouteNode(troublePointTmp.getLat(), troublePointTmp.getLng(),
                                troublePointTmp.getInternalEdgeId());
                        sendMessageAboutConstructionTrouble(); //send message to boss Agent
                    }
                    else {
                        logger.warn("Trouble point won't be generated because of too short path");
                    }

                    stop();
                }


                private int getFixedRandomIndex() {
                    // Warn: Value inside nextInt must be constant for fixed generation to work
                    var min = MAX_MOVE_ON_TP + configContainer.getConstructionSiteThresholdUntilIndexChange() + SAFE_BUFFER;
                    var max = initialRouteSize - min;
                    return getRandomIndexInBounds(min, max);
                }

                private int getTrulyRandomIndex(int moveIndex, int routeSize) {
                    var min = moveIndex + configContainer.getConstructionSiteThresholdUntilIndexChange() + SAFE_BUFFER;
                    var max = routeSize - min;
                    return getRandomIndexInBounds(min, max);
                }

                private int getRandomIndexInBounds(int min, int max) {
                    max = Math.max(max, 0);
                    var randomInt = random.nextInt(max + 1);
                    var index = min + (randomInt);
                    logger.debug("Index: " + index);

                    return index;
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

            addBehaviour(wrapErrors(troubleGenerator, onError));

        }
    }

    private void displayRouteDebug(List<RouteNode> route) {
        for (RouteNode node : route) {
            logger.info(node.getDebugString(node instanceof LightManagerNode) + "\n");
        }
    }

    public MovingObject getCar() {
        return car;
    }

    private void move() {
        if (borderlineIndex == null || car.getMoveIndex() < borderlineIndex) {
            car.move();
            if (!USE_BATCHED_UPDATES) {
                eventBus.post(new CarAgentUpdatedEvent(this.getId(), car.getPosition()));
            }
        }
    }

    public IGeoPosition getPosition() {
        return car.getPosition();
    }
}
