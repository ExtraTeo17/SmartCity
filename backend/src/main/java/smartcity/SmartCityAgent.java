package smartcity;

import agents.BikeAgent;
import agents.BusAgent;
import agents.CarAgent;
import agents.PedestrianAgent;
import agents.abstractions.IAgentsContainer;
import agents.utilities.MessageParameter;
import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import events.web.bike.BikeAgentDeadEvent;
import events.web.bus.BusAgentDeadEvent;
import events.web.car.CarAgentDeadEvent;
import events.web.pedestrian.PedestrianAgentDeadEvent;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import routing.RoutingConstants;
import vehicles.ITestable;
import vehicles.MovingObject;

import javax.annotation.Nullable;
import java.time.temporal.ChronoUnit;

public class SmartCityAgent extends Agent {
    public static final String name = SmartCityAgent.class.getSimpleName().replace("Agent", "");
    private static final Logger logger = LoggerFactory.getLogger(name);

    private final IAgentsContainer agentsContainer;
    private final EventBus eventBus;
    private int transformedTestBikeId = -1;
    private Long transformedPedestrianResultTime;
    private int transformedPedestrianDistance;

    @Inject
    SmartCityAgent(IAgentsContainer agentsContainer,
                   EventBus eventBus) {
        this.agentsContainer = agentsContainer;
        this.eventBus = eventBus;
    }

    @Override
    protected void setup() {
        addBehaviour(getReceiveMessageBehaviour());
    }

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
                            case MessageParameter.BIKE -> onReceiveBike(rcv);
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

    private void onReceivePedestrian(ACLMessage rcv) {
        var name = rcv.getSender().getLocalName();
        var agentOpt = agentsContainer.get(PedestrianAgent.class, (v) -> v.getLocalName().equals(name));
        if (agentOpt.isPresent()) {
            var agent = agentOpt.get();
            var pedestrian = agent.getPedestrian();

            Long resultTime = getTimeIfTestable(pedestrian);
            int distance = pedestrian.getDistance();
            var testBikeId = rcv.getUserDefinedParameter(MessageParameter.TEST_BIKE_AGENT_ID);
            var isMetamorphosis = !Strings.isNullOrEmpty(testBikeId);
            if (isMetamorphosis) {
                transformedTestBikeId = Integer.parseInt(testBikeId);
                transformedPedestrianResultTime = resultTime;
                transformedPedestrianDistance = pedestrian.getBeforeDistance();
            }

            if (agentsContainer.remove(agent)) {
                eventBus.post(new PedestrianAgentDeadEvent(agent.getId(), distance, isMetamorphosis ? null : resultTime));
            }
        }
    }

    private void onReceiveBike(ACLMessage rcv) {
        var name = rcv.getSender().getLocalName();
        var agentOpt = agentsContainer.get(BikeAgent.class, (v) -> v.getLocalName().equals(name));
        if (agentOpt.isPresent()) {
            var agent = agentOpt.get();
            var vehicle = agent.getVehicle();
            int distance = vehicle.getUniformRouteSize() * RoutingConstants.STEP_SIZE_METERS;
            Long resultTime = getTimeIfTestable(vehicle);
            if (agent.getId() == transformedTestBikeId) {
                resultTime += transformedPedestrianResultTime;
                distance += transformedPedestrianDistance;
            }

            if (agentsContainer.remove(agent)) {
                eventBus.post(new BikeAgentDeadEvent(agent.getId(), distance, resultTime));
            }
        }
    }

    private void onReceiveVehicle(ACLMessage rcv) {
        var name = rcv.getSender().getLocalName();
        var agentOpt = agentsContainer.get(CarAgent.class, (v) -> v.getLocalName().equals(name));
        if (agentOpt.isPresent()) {
            var agent = agentOpt.get();
            var vehicle = agent.getCar();

            Long resultTime = getTimeIfTestable(vehicle);
            int distance = vehicle.getUniformRouteSize() * RoutingConstants.STEP_SIZE_METERS;
            if (agentsContainer.remove(agent)) {
                eventBus.post(new CarAgentDeadEvent(agent.getId(), distance, resultTime));
            }
        }
    }

    @Nullable
    private Long getTimeIfTestable(MovingObject movingObject) {
        Long result = null;
        if (movingObject instanceof ITestable) {
            var testable = (ITestable) movingObject;
            result = ChronoUnit.SECONDS.between(testable.getStart(), testable.getEnd());
        }
        return result;
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
