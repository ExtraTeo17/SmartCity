package lightstrategies;

import agents.LightManager;
import agents.MessageParameter;
import gui.MapWindow;
import osmproxy.elements.OSMNode;
import smartcity.lights.Crossroad;
import smartcity.lights.OptimizationResult;
import smartcity.lights.SimpleCrossroad;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Properties;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.w3c.dom.Node;

import java.time.Instant;
import java.util.List;

public class LightManagerStrategy extends LightStrategy {

    private Crossroad crossroad;

    private LightManager agent;

    public LightManagerStrategy(Node crossroad, Long managerId) {
        this.crossroad = new SimpleCrossroad(crossroad, managerId);
    }

    public LightManagerStrategy(OSMNode centerCrossroadNode, long managerId) {
        this.crossroad = new SimpleCrossroad(centerCrossroadNode, managerId);
    }

    @Override
    public void ApplyStrategy(final LightManager agent) {
        crossroad.startLifetime();
        this.agent = agent;
        Behaviour communication = new CyclicBehaviour() {
            public void action() {
                ACLMessage rcv = agent.receive();
                if (rcv != null) {
                    handleMessageFromRecipient(rcv);
                }
                else {
                    block();
                }
            }

            private void handleMessageFromRecipient(ACLMessage rcv) {
                String recipientType = rcv.getUserDefinedParameter(MessageParameter.TYPE);
                switch (recipientType) {
                    case MessageParameter.VEHICLE:
                        handleMessageFromVehicle(rcv);
                        break;
                    case MessageParameter.PEDESTRIAN:
                        handleMessageFromPedestrian(rcv);
                        break;
                }
            }

            private void handleMessageFromVehicle(ACLMessage rcv) {
                switch (rcv.getPerformative()) {
                    case ACLMessage.INFORM:
                        Print(rcv.getSender().getLocalName() + " is approaching in " + getInstantParameter(rcv, MessageParameter.ARRIVAL_TIME) + "ms.");

                        crossroad.addCarToFarAwayQueue(getCarName(rcv),
                                getIntParameter(rcv, MessageParameter.ADJACENT_OSM_WAY_ID),
                                getInstantParameter(rcv, MessageParameter.ARRIVAL_TIME));

                        break;
                    case ACLMessage.REQUEST_WHEN:
                        Print(rcv.getSender().getLocalName() + " is waiting on way " + getIntParameter(rcv, MessageParameter.ADJACENT_OSM_WAY_ID) + ".");
                        crossroad.removeCarFromFarAwayQueue(getCarName(rcv),
                                getIntParameter(rcv, MessageParameter.ADJACENT_OSM_WAY_ID));
                        ACLMessage agree = new ACLMessage(ACLMessage.AGREE);
                        agree.addReceiver(rcv.getSender());
                        Properties properties = new Properties();
                        properties.setProperty(MessageParameter.TYPE, MessageParameter.LIGHT);
                        agree.setAllUserDefinedParameters(properties);
                        agent.send(agree);
                        // if (crossroad.isLightGreen(getIntParameter(rcv, ADJACENT_OSM_WAY_ID)))
                        //	answerCanProceed(getCarName(rcv),agent);
                        // else
                        crossroad.addCarToQueue(getCarName(rcv),
                                getIntParameter(rcv, MessageParameter.ADJACENT_OSM_WAY_ID));
                        break;
                    case ACLMessage.AGREE:
                        Print(rcv.getSender().getLocalName() + " passed the light.");
                        crossroad.removeCarFromQueue(getIntParameter(rcv, MessageParameter.ADJACENT_OSM_WAY_ID));
                        break;
                    default:
                        System.out.println("Wait");
                }
            }

            private void handleMessageFromPedestrian(ACLMessage rcv) {
                switch (rcv.getPerformative()) {
                    case ACLMessage.INFORM:
                        Print(rcv.getSender().getLocalName() + " is approaching in " + getInstantParameter(rcv, MessageParameter.ARRIVAL_TIME) + "ms.");

                        crossroad.addPedestrianToFarAwayQueue(rcv.getSender().getLocalName(),
                                getIntParameter(rcv, MessageParameter.ADJACENT_OSM_WAY_ID),
                                getInstantParameter(rcv, MessageParameter.ARRIVAL_TIME));

                    case ACLMessage.REQUEST_WHEN:
                        Print(rcv.getSender().getLocalName() + " is waiting on way " + getIntParameter(rcv, MessageParameter.ADJACENT_OSM_WAY_ID) + ".");
                        crossroad.removePedestrianFromFarAwayQueue(rcv.getSender().getLocalName(),
                                getIntParameter(rcv, MessageParameter.ADJACENT_OSM_WAY_ID));
                        ACLMessage agree = new ACLMessage(ACLMessage.AGREE);
                        agree.addReceiver(rcv.getSender());
                        Properties properties = new Properties();
                        properties.setProperty(MessageParameter.TYPE, MessageParameter.LIGHT);
                        agree.setAllUserDefinedParameters(properties);
                        agent.send(agree);

                        crossroad.addPedestrianToQueue(rcv.getSender().getLocalName(),
                                getIntParameter(rcv, MessageParameter.ADJACENT_OSM_WAY_ID));
                        break;
                    case ACLMessage.AGREE:
                        Print(rcv.getSender().getLocalName() + " passed the light.");
                        crossroad.removePedestrianFromQueue(getIntParameter(rcv, MessageParameter.ADJACENT_OSM_WAY_ID));
                    default:
                        Print("Wait");
                }
            }
        };

        Behaviour checkState = new TickerBehaviour(agent, 100 / MapWindow.getTimeScale()) {

            @Override
            protected void onTick() {
                //for all Light check
                //check if time from last green > written time
                // if so, put in the queue
                //if not
                // check count of people (rememeber about 2 person on pedestrian light= 1 car)
                // if queue is empty
                // apply strategy
                //for elemnts in queue (if there are elements in queue, make green)
                //System.out.println("Optimization");
                OptimizationResult result = crossroad.requestOptimizations();
                //System.out.println("Len: " + result.carsFreeToProceed().size());
                handleOptimizationResult(result);
            }

            private void handleOptimizationResult(OptimizationResult result) {
                List<String> carNames = result.carsFreeToProceed();
                for (String carName : carNames) {
                    answerCanProceed(carName, agent);
                }
            }

        };

        agent.addBehaviour(communication);
        agent.addBehaviour(checkState);
    }

    private void answerCanProceed(String carName, Agent agent) {
        Print(carName + " can proceed.");
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(new AID(carName, AID.ISLOCALNAME));
        Properties properties = new Properties();
        properties.setProperty(MessageParameter.TYPE, MessageParameter.LIGHT);
        msg.setAllUserDefinedParameters(properties);
        agent.send(msg);
    }

    private int getIntParameter(ACLMessage rcv, String param) {
        return Integer.parseInt(rcv.getUserDefinedParameter(param));
    }

    private Instant getInstantParameter(ACLMessage rcv, String param) {
        return Instant.parse(rcv.getUserDefinedParameter(param));
    }

    private String getCarName(ACLMessage rcv) {
        return rcv.getSender().getLocalName();
    }

    public void drawCrossroad(List<Painter<JXMapViewer>> painter) {
        crossroad.draw(painter);
    }

    public void Print(String message) {
        System.out.println(agent.getLocalName() + ": " + message);
    }
}