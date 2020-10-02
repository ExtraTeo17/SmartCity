package smartcity;

import agents.BusAgent;
import agents.PedestrianAgent;
import agents.VehicleAgent;
import agents.abstractions.IAgentsContainer;
import agents.utilities.MessageParameter;
import com.google.inject.Inject;
import gui.MapWindow;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import routing.LightManagerNode;
import routing.StationNode;
import vehicles.TestCar;
import vehicles.TestPedestrian;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// TODO: This class should have no more than 10 fields.
// TODO: This class should be package private
public class MasterAgent extends Agent {
    public static final String name = MasterAgent.class.getName().replace("Agent", "");
    private static final Logger logger = LoggerFactory.getLogger(MasterAgent.class);

    private final MapWindow window;
    private final IAgentsContainer agentsContainer;

    // TODO: Delete this
    @Deprecated(forRemoval = true, since = "Always - Eldritch Abomination")
    public static Map<Pair<Long, Long>, LightManagerNode> wayIdLightIdToLightManagerNode = new HashMap<>();
    @Deprecated(forRemoval = true, since = "Always - Eldritch Abomination")
    public static Map<Long, LightManagerNode> crossingOsmIdToLightManagerNode = new HashMap<>();
    @Deprecated(forRemoval = true, since = "Always - Eldritch Abomination")
    public static Map<Long, StationNode> osmStationIdToStationNode = new HashMap<>();

    @Inject
    public MasterAgent(IAgentsContainer agentsContainer,
                       MapWindow window) {
        this.agentsContainer = agentsContainer;
        this.window = window;
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
                    // TODO: Does it work?? (can't see it in the logs)
                    logger.info("SmartCity: " + rcv.getSender().getLocalName() + " arrived at destination.");
                    String type = rcv.getUserDefinedParameter(MessageParameter.TYPE);
                    switch (type) {
                        case MessageParameter.VEHICLE -> onReceiveVehicle(rcv);
                        case MessageParameter.PEDESTRIAN -> onReceivePedestrian(rcv);
                        case MessageParameter.BUS -> onReceiveBus(rcv);
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
            if (pedestrian instanceof TestPedestrian) {
                var testPedestrian = (TestPedestrian) pedestrian;
                setResultTime(testPedestrian.getStart(), testPedestrian.getEnd());
            }

            agentsContainer.remove(agent);
        }
    }

    private void setResultTime(LocalDateTime start, LocalDateTime end) {
        long seconds = Duration.between(start, end).getSeconds();
        String time = String.format(
                "%d:%02d:%02d",
                seconds / 3600,
                (seconds % 3600) / 60,
                seconds % 60);
        // TODO: Push message to gui
        window.setResultTime(time);
    }

    private void onReceiveVehicle(ACLMessage rcv) {
        var name = rcv.getSender().getLocalName();
        var agentOpt = agentsContainer.get(VehicleAgent.class, (v) -> v.getLocalName().equals(name));
        if (agentOpt.isPresent()) {
            var agent = agentOpt.get();
            var vehicle = agent.getVehicle();
            if (vehicle instanceof TestCar) {
                var testVehicle = (TestCar) vehicle;
                setResultTime(testVehicle.getStart(), testVehicle.getEnd());
            }

            agentsContainer.remove(agent);
        }
    }

    private void onReceiveBus(ACLMessage rcv) {
        agentsContainer.removeIf(BusAgent.class,
                v -> v.getLocalName().equals(rcv.getSender().getLocalName()));
    }
}
