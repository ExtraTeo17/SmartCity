package agents;

import agents.abstractions.IAgentsContainer;
import agents.utilities.MessageParameter;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import events.web.PrepareSimulationEvent;
import events.web.TroublePointCreatedEvent;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import routing.core.Position;

import static agents.message.MessageManager.createProperties;

public class TroubleManagerAgent extends Agent {
    public static final String name = TroubleManagerAgent.class.getSimpleName().replace("Agent", "");
    private final static Logger logger = LoggerFactory.getLogger(TroubleManagerAgent.class);

    private final IAgentsContainer agentsContainer;
    private final EventBus eventBus;

    @Inject
    TroubleManagerAgent(IAgentsContainer agentsContainer,
                        EventBus eventBus) {
        this.agentsContainer = agentsContainer;
        this.eventBus = eventBus;
    }


    @Override
    protected void setup() {
        super.setup();
        addBehaviour(communication);
    }

    //TODO: Broadcast on trouble end
    Behaviour communication = new CyclicBehaviour() {
        @Override
        public void action() {
            ACLMessage rcv = receive();
            if (rcv != null) {
                switch (rcv.getPerformative()) {
                    case ACLMessage.INFORM -> {

                        // parsing received message
                        //TODO: Show trouble point on gui
                        var troublePoint = Position.of(Double.parseDouble(rcv.getUserDefinedParameter(MessageParameter.TROUBLE_LAT)),
                                Double.parseDouble(rcv.getUserDefinedParameter(MessageParameter.TROUBLE_LON)));
                        // TODO: Generate id and save it to hide troublePoint later
                        eventBus.post(new TroublePointCreatedEvent(1, troublePoint));
                        logger.info("TroubleAgent: Got message about trouble");
                        logger.info("troublePoint: " + troublePoint.getLat() + "  " + troublePoint.getLng());
                        var edgeId = Long.parseLong(rcv.getUserDefinedParameter(MessageParameter.EDGE_ID));
                        logger.info("trouble edge: " + edgeId);


                        //broadcasting to everybody
                        ACLMessage response = new ACLMessage(ACLMessage.PROPOSE);
                        Properties properties = createProperties(MessageParameter.TROUBLE_MANAGER);
                        properties.setProperty(MessageParameter.EDGE_ID, Long.toString(edgeId));
                        response.setAllUserDefinedParameters(properties);

                        agentsContainer.forEach(VehicleAgent.class, vehicleAgent -> {
                            response.addReceiver(vehicleAgent.getAID());
                        });
                        send(response);
                        logger.info("Send broadcast");

                    }
                }
                block(100);
            }
        }
    };

    // for tests
    @Subscribe
    public void handle(PrepareSimulationEvent e){
        var troublePoint = Position.of(52.23682, 21.01683);
        eventBus.post(new TroublePointCreatedEvent(1, troublePoint));
    }
}
