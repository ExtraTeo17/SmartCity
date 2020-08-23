package behaviourfactories;

import agents.LightManager;
import agents.utilities.MessageParameter;
import gui.MapWindow;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartcity.TimeManager;
import smartcity.lights.OptimizationResult;

import java.time.Instant;
import java.util.List;

public class LightManagerBehaviourFactory implements IBehaviourFactory<LightManager> {
    private static final Logger logger = LoggerFactory.getLogger(LightManagerBehaviourFactory.class);

    @Override
    public CyclicBehaviour createCyclicBehaviour(final LightManager agent) {
        return new CyclicBehaviour() {
            @Override
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
                    case MessageParameter.VEHICLE -> handleMessageFromVehicle(rcv);
                    case MessageParameter.PEDESTRIAN -> handleMessageFromPedestrian(rcv);
                }
            }

            private void handleMessageFromVehicle(ACLMessage rcv) {
                // TODO: Should be refactored - too much usage of crossroad methods.
                var crossroad = agent.getCrossroad();
                switch (rcv.getPerformative()) {
                    case ACLMessage.INFORM -> {
                        print(agent, rcv.getSender().getLocalName() + " is approaching in " + getInstantParameter(rcv,
                                MessageParameter.ARRIVAL_TIME) + "ms.");
                        crossroad.addCarToFarAwayQueue(getCarName(rcv),
                                getIntParameter(rcv, MessageParameter.ADJACENT_OSM_WAY_ID),
                                getInstantParameter(rcv, MessageParameter.ARRIVAL_TIME));
                    }
                    case ACLMessage.REQUEST_WHEN -> {
                        print(agent, rcv.getSender().getLocalName() + " is waiting on way " + getIntParameter(rcv,
                                MessageParameter.ADJACENT_OSM_WAY_ID) + ".");
                        crossroad.removeCarFromFarAwayQueue(getCarName(rcv), getIntParameter(rcv,
                                MessageParameter.ADJACENT_OSM_WAY_ID));
                        ACLMessage agree = new ACLMessage(ACLMessage.AGREE);
                        agree.addReceiver(rcv.getSender());
                        Properties properties = new Properties();
                        properties.setProperty(MessageParameter.TYPE, MessageParameter.LIGHT);
                        agree.setAllUserDefinedParameters(properties);
                        agent.send(agree);
                        crossroad.addCarToQueue(getCarName(rcv),
                                getIntParameter(rcv, MessageParameter.ADJACENT_OSM_WAY_ID));
                    }
                    case ACLMessage.AGREE -> {
                        print(agent, rcv.getSender().getLocalName() + " passed the light.");
                        crossroad.removeCarFromQueue(getIntParameter(rcv, MessageParameter.ADJACENT_OSM_WAY_ID));
                    }
                    default -> logger.info("Wait");
                }
            }

            private void handleMessageFromPedestrian(ACLMessage rcv) {
                // TODO: Should be refactored - too much usage of crossroad methods.
                var crossroad = agent.getCrossroad();
                switch (rcv.getPerformative()) {
                    case ACLMessage.INFORM:
                        print(agent, rcv.getSender().getLocalName() + " is approaching in " + getInstantParameter(rcv,
                                MessageParameter.ARRIVAL_TIME) + "ms.");

                        crossroad.addPedestrianToFarAwayQueue(rcv.getSender().getLocalName(),
                                getIntParameter(rcv, MessageParameter.ADJACENT_OSM_WAY_ID),
                                getInstantParameter(rcv, MessageParameter.ARRIVAL_TIME));

                    case ACLMessage.REQUEST_WHEN:
                        print(agent, rcv.getSender().getLocalName() + " is waiting on way " + getIntParameter(rcv,
                                MessageParameter.ADJACENT_OSM_WAY_ID) + ".");
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
                        print(agent, rcv.getSender().getLocalName() + " passed the light.");
                        crossroad.removePedestrianFromQueue(getIntParameter(rcv, MessageParameter.ADJACENT_OSM_WAY_ID));
                    default:
                        print(agent, "Wait");
                }
            }
        };

    }

    @Override
    public TickerBehaviour createTickerBehaviour(final LightManager agent) {
        return new TickerBehaviour(agent, 100 / TimeManager.TIME_SCALE) {
            @Override
            protected void onTick() {
                //for all Light check
                //check if time from last green > written time
                // if so, put in the queue
                //if not
                // check count of people (remember about 2 person on pedestrian light= 1 car)
                // if queue is empty
                // apply strategy
                //for elements in queue (if there are elements in queue, make green)
                var crossroad = agent.getCrossroad();
                OptimizationResult result = crossroad.requestOptimizations();
                handleOptimizationResult(result);
            }

            private void handleOptimizationResult(OptimizationResult result) {
                List<String> carNames = result.carsFreeToProceed();
                for (String carName : carNames) {
                    answerCanProceed(carName, agent);
                }
            }

        };
    }

    private void answerCanProceed(String carName, Agent agent) {
        print(agent, carName + " can proceed.");
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

    public void print(Agent agent, String message) {
        logger.info(agent.getLocalName() + ": " + message);
    }
}
