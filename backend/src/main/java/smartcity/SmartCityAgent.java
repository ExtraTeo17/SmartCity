package smartcity;

import agents.BusAgent;
import agents.PedestrianAgent;
import agents.VehicleAgent;
import agents.abstractions.IAgentsContainer;
import agents.utilities.MessageParameter;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import events.web.bus.BusAgentDeadEvent;
import events.web.pedestrian.PedestrianAgentDeadEvent;
import events.web.vehicle.VehicleAgentDeadEvent;
import gui.MapWindow;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import routing.RoutingConstants;
import vehicles.ITestable;
import vehicles.MovingObject;
import vehicles.TestPedestrian;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class SmartCityAgent extends Agent {
    public static final String name = SmartCityAgent.class.getSimpleName().replace("Agent", "");
    private static final Logger logger = LoggerFactory.getLogger(SmartCityAgent.class);

    private final MapWindow window;
    private final IAgentsContainer agentsContainer;
    private final EventBus eventBus;

    @Inject
    SmartCityAgent(IAgentsContainer agentsContainer,
                   MapWindow window,
                   EventBus eventBus) {
        this.agentsContainer = agentsContainer;
        this.window = window;
        this.eventBus = eventBus;
    }

    @Override
    protected void setup() {
        window.display();
        addBehaviour(getReceiveMessageBehaviour());
    }

    // TODO: Set simulationState to Finished when no longer needed and post event
    private CyclicBehaviour getReceiveMessageBehaviour() {
        return new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage rcv = receive();
                if (rcv != null) {
                    logger.info(rcv.getSender().getLocalName() + " arrived at destination.");
                    String type = rcv.getUserDefinedParameter(MessageParameter.TYPE);
                    if (type == null) {
                        logger.warn("Received message from" + rcv.getSender() + " without type:" + rcv);
                        return;
                    }
                    try {
                        switch (type) {
                            case MessageParameter.VEHICLE -> onReceiveVehicle(rcv);
                            case MessageParameter.PEDESTRIAN -> onReceivePedestrian(rcv);
                            case MessageParameter.BUS -> onReceiveBus(rcv);
                        }
                    } catch (Exception e) {
                        logger.warn("Unknown error", e);
                    }
                }
                block(1000);
            }
        };
    }

    // TODO: Almost the same as ReceiveVehicle - merge when TestPedestrian/TestCar will have common Interface
    private void onReceivePedestrian(ACLMessage rcv) {
        var name = rcv.getSender().getLocalName();
        var agentOpt = agentsContainer.get(PedestrianAgent.class, (v) -> v.getLocalName().equals(name));
        if (agentOpt.isPresent()) {
            var agent = agentOpt.get();
            var pedestrian = agent.getPedestrian();
            if (pedestrian instanceof ITestable) {
                var testPedestrian = (ITestable) pedestrian;
                // getTimeIfTestable(testPedestrian.getStart(), testPedestrian.getEnd());
            }

            if (agentsContainer.remove(agent)) {
                eventBus.post(new PedestrianAgentDeadEvent(agent.getId()));
            }
        }
    }


    private void onReceiveVehicle(ACLMessage rcv) {
        var name = rcv.getSender().getLocalName();
        var agentOpt = agentsContainer.get(VehicleAgent.class, (v) -> v.getLocalName().equals(name));
        if (agentOpt.isPresent()) {
            var agent = agentOpt.get();
            var vehicle = agent.getVehicle();

            Long resultTime = getTimeIfTestable(vehicle);
            int distance = vehicle.getUniformRouteSize() * RoutingConstants.STEP_SIZE_METERS;
            if (agentsContainer.remove(agent)) {
                eventBus.post(new VehicleAgentDeadEvent(agent.getId(), distance, resultTime));
            }
        }
    }

    private Long getTimeIfTestable(MovingObject movingObject) {
        if (movingObject instanceof ITestable) {
            var testable = (ITestable) movingObject;
            return ChronoUnit.SECONDS.between(testable.getStart(), testable.getEnd());
        }
        return null;
    }

    private void onReceiveBus(ACLMessage rcv) {
        var senderName = rcv.getSender().getLocalName();
        var agentOpt = agentsContainer.get(BusAgent.class, (v) -> senderName.equals(v.getLocalName()));
        if (agentOpt.isPresent()) {
            var agent = agentOpt.get();
            if (agentsContainer.remove(agent)) {
                eventBus.post(new BusAgentDeadEvent(agent.getId()));
            }
        }
    }
}
