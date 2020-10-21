package agents;

import agents.abstractions.AbstractAgent;
import agents.utilities.MessageParameter;
import com.google.common.eventbus.EventBus;
import events.web.vehicle.VehicleAgentRouteChangedEvent;
import events.web.vehicle.VehicleAgentUpdatedEvent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Properties;
import osmproxy.ExtendedGraphHopper;
import routing.abstractions.IRouteGenerator;
import routing.abstractions.IRouteTransformer;
import routing.core.IGeoPosition;
import routing.nodes.LightManagerNode;
import routing.nodes.RouteNode;
import smartcity.ITimeProvider;
import smartcity.SmartCityAgent;
import vehicles.MovingObject;
import vehicles.enums.DrivingState;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static agents.message.MessageManager.createMessage;
import static agents.message.MessageManager.createProperties;
import static routing.RoutingConstants.STEP_CONSTANT;

@SuppressWarnings("serial")
// TODO: Maybe rename to CarAgent? Bus is also a Vehicle
public class VehicleAgent extends AbstractAgent {
    private static final Random random = new Random();
    private static final int THRESHOLD_UNTIL_INDEX_CHANGE = 3;

    private final MovingObject vehicle;
    private final int timeBeforeAccident;
    private final IRouteGenerator routeGenerator;
    private final IRouteTransformer routeTransformer;
    private RouteNode troublePoint;

    VehicleAgent(int id, MovingObject vehicle, int timeBeforeAccident,
                 ITimeProvider timeProvider,
                 IRouteGenerator routeGenerator,
                 IRouteTransformer routeTransformer,
                 EventBus eventBus) {
        super(id, vehicle.getVehicleType(), timeProvider, eventBus);
        this.vehicle = vehicle;
        this.timeBeforeAccident = timeBeforeAccident;
        this.routeGenerator = routeGenerator;
        this.routeTransformer = routeTransformer;
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
                            print("Asking LightManager" + light.getLightManagerId() + " for right to passage.");
                            break;
                        case WAITING_AT_LIGHT:

                            break;
                        case PASSING_LIGHT:
                            print("Passing");
                            move();
                            vehicle.setState(DrivingState.MOVING);
                            break;
                    }
                }
                else if (vehicle.isAtDestination()) {
                    vehicle.setState(DrivingState.AT_DESTINATION);
                    print("Reached destination.");

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

                            Long edgeId = Long.parseLong(rcv.getUserDefinedParameter(MessageParameter.EDGE_ID));
                            logger.info("  GOT PROPOSE TO CHANGE THE ROUTE. AND EXCLUDE: " + edgeId);
                            if (vehicle.checkIfEdgeExistsAndFarEnough(edgeId)) {
                                logger.info("CAR:EDGE EXISTS");

                                RouteNode routeCarOnThreshold = vehicle.getPositionFarOnIndex(THRESHOLD_UNTIL_INDEX_CHANGE);
                                int indexAfterWhichRouteChange = vehicle.getFarOnIndex(THRESHOLD_UNTIL_INDEX_CHANGE);

                                if (!vehicle.currentTrafficLightNodeWithinAlternativeRouteThreshold(THRESHOLD_UNTIL_INDEX_CHANGE)) {
                                    sendRefusalMessageToLightManagerAfterRouteChange(THRESHOLD_UNTIL_INDEX_CHANGE);
                                }

                                var oldUniformRoute = vehicle.getUniformRoute();

                                displayTheRoute(oldUniformRoute);

                                var newSimpleRouteEnd = routeGenerator.generateRouteInfo(routeCarOnThreshold,
                                        oldUniformRoute.get(oldUniformRoute.size() - 1));
                                var newRouteAfterChangeIndex = routeTransformer.uniformRoute(newSimpleRouteEnd);

                                var route = oldUniformRoute.subList(0, indexAfterWhichRouteChange);
                                route.addAll(newRouteAfterChangeIndex);
                                var mergeResult = routeTransformer.mergeByDistance(vehicle.getSimpleRoute(),
                                        newSimpleRouteEnd);
                                vehicle.setRoutes(mergeResult.mergedRoute, route);

                                displayTheRoute(route);

                                vehicle.switchToNextTrafficLight();

                                eventBus.post(new VehicleAgentRouteChangedEvent(getId(), mergeResult.startNodes, routeCarOnThreshold,
                                        newSimpleRouteEnd));
                            }

                        }
                    }
                }
                block(100);

            }

            private void displayTheRoute(List<RouteNode> uniformRoute) {
                for (int i = 0; i < uniformRoute.size(); ++i) {
                    if (uniformRoute.get(i) instanceof LightManagerNode) {
                        System.out.print("LMN");
                    }
                    else {
                        System.out.print("RN");
                    }
                    if (vehicle.getMoveIndex() == i) {
                        System.out.print("+US");
                    }
                    System.out.print("  ");
                }
                System.out.println();
            }

            private void sendRefusalMessageToLightManagerAfterRouteChange(int howFar) {
                //change route, that is why send stop
                LightManagerNode currentManager = vehicle.getCurrentTrafficLightNode();
                if (currentManager != null) {
                    //TODO:check if it is correct
                    ACLMessage msg = createMessage(ACLMessage.REFUSE, LightManagerAgent.name + currentManager.getLightManagerId());
                    Properties properties = createProperties(MessageParameter.VEHICLE);
                    properties.setProperty(MessageParameter.ADJACENT_OSM_WAY_ID, Long.toString(currentManager.getAdjacentWayId()));
                    send(msg);
                    print("Send REFUSE to LightManager" + currentManager.getLightManagerId() + ".");
                }
            }
        };

        Behaviour troubleGenerator = new TickerBehaviour(this, this.timeBeforeAccident) {
            @Override
            public void onTick() {
                var route = vehicle.getUniformRoute();

                //TODO: from current index
                //choose trouble EdgeId
                var el = random.nextInt(route.size());
                RouteNode troublePointTmp = route.get(el);
                troublePoint = new RouteNode(troublePointTmp.getLat(), troublePointTmp.getLng(), troublePointTmp.getInternalEdgeId());

                //send message to boss Agent
                sendMessageAboutTrouble();

                ExtendedGraphHopper.addForbiddenEdges(Arrays.asList(troublePoint.getInternalEdgeId()));
                stop();
            }

            private void sendMessageAboutTrouble() {

                ACLMessage msg = createMessage(ACLMessage.INFORM, TroubleManagerAgent.name);
                Properties properties = createProperties(MessageParameter.VEHICLE);
                properties.setProperty(MessageParameter.TROUBLE, MessageParameter.SHOW);
                properties.setProperty(MessageParameter.TROUBLE_LAT, Double.toString(troublePoint.getLat()));
                properties.setProperty(MessageParameter.TROUBLE_LON, Double.toString(troublePoint.getLng()));

                properties.setProperty(MessageParameter.EDGE_ID, Long.toString(troublePoint.getInternalEdgeId()));
                msg.setAllUserDefinedParameters(properties);
                print(" send message about trouble on " + Long.toString(troublePoint.getInternalEdgeId()));
                send(msg);
            }


        };

        Behaviour troubleStopper = new TickerBehaviour(this, 3 * this.timeBeforeAccident) {
            @Override
            public void onTick() {

                sendMessageAboutTroubleStop();
                ExtendedGraphHopper.removeForbiddenEdges(Arrays.asList(troublePoint.getInternalEdgeId()));
                stop();
            }

            private void sendMessageAboutTroubleStop() {

                ACLMessage msg = createMessage(ACLMessage.INFORM, TroubleManagerAgent.name);
                Properties properties = createProperties(MessageParameter.VEHICLE);
                properties.setProperty(MessageParameter.TROUBLE, MessageParameter.STOP);

                properties.setProperty(MessageParameter.TROUBLE_LAT, Double.toString(troublePoint.getLat()));
                properties.setProperty(MessageParameter.TROUBLE_LON, Double.toString(troublePoint.getLng()));

                properties.setProperty(MessageParameter.EDGE_ID, Long.toString(troublePoint.getInternalEdgeId()));
                msg.setAllUserDefinedParameters(properties);
                print(" send message about trouble stop on " + Long.toString(troublePoint.getInternalEdgeId()));
                send(msg);
            }

        };

        addBehaviour(move);
        addBehaviour(communication);
        addBehaviour(troubleGenerator);
        addBehaviour(troubleStopper);
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
