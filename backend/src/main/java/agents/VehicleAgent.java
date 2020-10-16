package agents;

import agents.abstractions.AbstractAgent;
import agents.utilities.MessageParameter;
import com.google.common.eventbus.EventBus;
import events.web.VehicleAgentUpdatedEvent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Properties;
import routing.abstractions.IRouteGenerator;
import routing.abstractions.IRouteTransformer;
import routing.core.IGeoPosition;
import routing.nodes.LightManagerNode;
import routing.nodes.RouteNode;
import smartcity.ITimeProvider;
import smartcity.SmartCityAgent;
import vehicles.Car;
import vehicles.DrivingState;
import vehicles.MovingObject;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static agents.message.MessageManager.createMessage;
import static agents.message.MessageManager.createProperties;
import static routing.RoutingConstants.STEP_CONSTANT;

@SuppressWarnings("serial")
// TODO: Maybe rename to CarAgent? Bus is also a Vehicle
public class VehicleAgent extends AbstractAgent {
	
	private final int THRESHOLD_UNTIL_INDEX_CHANGE = 3;
	
    private final MovingObject vehicle;
    private  int timeBeforeAccident = 0;
    private IRouteGenerator  routeGenerator;
    private  RouteNode troublePoint;
    private IRouteTransformer routeTransformer;

    VehicleAgent(int id, MovingObject vehicle, ITimeProvider timeProvider, EventBus eventBus,
    		int timeBeforeAccident, IRouteGenerator routeGenerator, IRouteTransformer routeTransformer) {
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
                            LightManagerNode light = vehicle.switchToNextTrafficLight();
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
                            System.out.println(myAgent.getLocalName() + "  GOT PROPOSE TO CHANGE THE ROUTE. AND EXCLUDE: " + edgeId);
                            if (vehicle.checkIfEdgeExistsAndFarEnough(edgeId)) {
                                System.out.println("CAR:EDGE EXISTS");

                                RouteNode routeCarOnThreshold = vehicle.getPositionFarOnIndex(THRESHOLD_UNTIL_INDEX_CHANGE);
                                int indexAfterWhichRouteChange = vehicle.getFarOnIndex(THRESHOLD_UNTIL_INDEX_CHANGE);
                                sendMessageToLightManager(indexAfterWhichRouteChange,THRESHOLD_UNTIL_INDEX_CHANGE);
                                List<RouteNode> uniformRoute = vehicle.getUniformRoute();
                                var newRouteAfterChangeIndex = routeTransformer.uniformRoute(
                                		routeGenerator.generateRouteInfo(routeCarOnThreshold,
                                				uniformRoute.get(uniformRoute.size() - 1)));
                                var route = uniformRoute.subList(0, indexAfterWhichRouteChange);
                                route.addAll(newRouteAfterChangeIndex);
                                vehicle.setUniformRoute(route);
                                
                                //TODO: change route in the GUI and replace the old one PRZEMEK
                            }

                        }
                    }
                }
                block(100);

            }
            private void sendMessageToLightManager(int indexAfterWhichRouteChange,int howFar) {
                //change route, that is why send stop
                LightManagerNode nextManager = vehicle.switchToNextTrafficLight(indexAfterWhichRouteChange);
                if (nextManager != null) {
                    //TODO:check if it is correct
                    ACLMessage msg = createMessage(ACLMessage.REFUSE, LightManagerAgent.name+ nextManager.getLightManagerId());
                    Properties properties = createProperties(MessageParameter.VEHICLE);
                    properties.setProperty(MessageParameter.ADJACENT_OSM_WAY_ID, Long.toString(vehicle.getAdjacentOsmWayId(howFar)));
                    send(msg);
                    print("Send REFUSE to LightManager" + nextManager.getLightManagerId() + ".");
                }
            }
        };
        Behaviour troubleGenerator = new TickerBehaviour(this, this.timeBeforeAccident) {
            @Override
            public void onTick() {

                List route = vehicle.getUniformRoute();

                //TODO: from current index
                Random random = new Random();
                //choose trouble EdgeId
                var el = random.nextInt(route.size());

                RouteNode troublePointTmp =  vehicle.getUniformRoute().get(el);
                troublePoint = new RouteNode(troublePointTmp.getLat(),troublePointTmp.getLng(),troublePointTmp.getInternalEdgeId());

                //send message to boss Agent
                sendMessageAboutTrouble();

                osmproxy.ExtendedGraphHopper.addForbiddenEdges(Arrays.asList(troublePoint.getInternalEdgeId()));
                stop();
            }
            
            private void sendMessageAboutTrouble() {

                ACLMessage msg = createMessage(ACLMessage.INFORM, TroubleManagerAgent.name);
                Properties properties = createProperties(MessageParameter.VEHICLE);
                properties.setProperty(MessageParameter.TROUBLE,MessageParameter.SHOW);
                properties.setProperty(MessageParameter.TROUBLE_LAT, Double.toString(troublePoint.getLat()));
                properties.setProperty(MessageParameter.TROUBLE_LON, Double.toString(troublePoint.getLng()));

                properties.setProperty(MessageParameter.EDGE_ID, Long.toString(troublePoint.getInternalEdgeId()));
                msg.setAllUserDefinedParameters(properties);
                print(" send message about trouble on " + Long.toString(troublePoint.getInternalEdgeId()));
                send(msg);
            }
        };
        
        Behaviour troubleStopper = new TickerBehaviour(this, 3*this.timeBeforeAccident) {
            @Override
            public void onTick() {

                sendMessageAboutTroubleStop();
                osmproxy.ExtendedGraphHopper.removeForbiddenEdges(Arrays.asList(troublePoint.getInternalEdgeId()));
                stop();
            }
            private void sendMessageAboutTroubleStop() {

                ACLMessage msg = createMessage(ACLMessage.INFORM, TroubleManagerAgent.name);
                Properties properties = createProperties(MessageParameter.VEHICLE);
                properties.setProperty(MessageParameter.TROUBLE,MessageParameter.STOP);

                properties.setProperty(MessageParameter.TROUBLE_LAT, Double.toString(troublePoint.getLat()));
                properties.setProperty(MessageParameter.TROUBLE_LON, Double.toString(troublePoint.getLng()));

                properties.setProperty(MessageParameter.EDGE_ID, Long.toString(troublePoint.getInternalEdgeId()));
                msg.setAllUserDefinedParameters(properties);
                System.out.println(myAgent.getLocalName() + " send message about trouble stop on " +Long.toString(troublePoint.getInternalEdgeId()));
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
