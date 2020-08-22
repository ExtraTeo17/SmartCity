package smartcity;

import agents.*;
import agents.abstractions.AbstractAgent;
import agents.abstractions.IAgentsContainer;
import agents.utilities.MessageParameter;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import events.LightManagersReadyEvent;
import events.PrepareSimulationEvent;
import events.StartSimulationEvent;
import gui.MapWindow;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import osmproxy.LightAccessManager;
import osmproxy.MapAccessManager;
import osmproxy.buses.IBusLinesManager;
import osmproxy.elements.OSMNode;
import osmproxy.elements.OSMStation;
import routing.LightManagerNode;
import routing.RouteNode;
import routing.StationNode;
import smartcity.buses.Timetable;
import smartcity.config.ConfigContainer;
import smartcity.task.TaskManager;
import vehicles.Bus;
import vehicles.Pedestrian;
import vehicles.TestCar;
import vehicles.TestPedestrian;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

// TODO: This class should have no more than 10 fields.
// TODO: This class should be package private
public class MasterAgent extends Agent {
    public static final String name = MasterAgent.class.getName().replace("Agent", "");
    private static final Logger logger = LoggerFactory.getLogger(MasterAgent.class);

    private static AgentContainer container;
    private static MapWindow window;
    private final IBusLinesManager busLinesManager;
    private final IdGenerator idGenerator;
    private final IAgentsContainer agentsContainer;
    private final TaskManager taskManager;
    private final LightAccessManager lightAccessManager;
    private final ConfigContainer configContainer;
    private final EventBus eventBus;

    // TODO: Delete this abomination (or at least make it private)
    public static final List<PedestrianAgent> pedestrians = new CopyOnWriteArrayList<>();

    public static Map<Pair<Long, Long>, LightManagerNode> wayIdLightIdToLightManagerNode = new HashMap<>();
    public static Map<Long, LightManagerNode> crossingOsmIdToLightManagerNode = new HashMap<>();
    public static Map<Long, StationNode> osmStationIdToStationNode = new HashMap<>();
    public static Map<Long, OSMStation> osmIdToStationOSMNode = new HashMap<>();

    @Inject
    public MasterAgent(IBusLinesManager busLinesManager,
                       IdGenerator idGenerator,
                       IAgentsContainer agentsContainer,
                       TaskManager taskManager,
                       LightAccessManager lightAccessManager,
                       ConfigContainer configContainer,
                       EventBus eventBus,
                       MapWindow window) {
        this.busLinesManager = busLinesManager;
        this.idGenerator = idGenerator;
        this.agentsContainer = agentsContainer;
        this.taskManager = taskManager;
        this.lightAccessManager = lightAccessManager;
        this.configContainer = configContainer;
        this.eventBus = eventBus;

        // TODO: Delete this abomination
        MasterAgent.window = window;
    }

    @Override
    protected void setup() {
        container = getContainerController();
        window.display();

        addBehaviour(getReceiveMessageBehaviour());
    }

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
                        case MessageParameter.BUS -> agentsContainer.removeIf(BusAgent.class,
                                v -> v.getLocalName().equals(rcv.getSender().getLocalName()));
                    }
                }
                block(1000);
            }
        };
    }

    private void onReceivePedestrian(ACLMessage rcv) {
        for (int i = 0; i < pedestrians.size(); i++) {
            PedestrianAgent v = pedestrians.get(i);
            if (v.getLocalName().equals(rcv.getSender().getLocalName())) {
                if (v.getPedestrian() instanceof TestPedestrian) {
                    TestPedestrian pedestrian = (TestPedestrian) v.getPedestrian();
                    setResultTime(pedestrian.start, pedestrian.end);
                }
                pedestrians.remove(i);
                break;
            }
        }
    }

    private void setResultTime(Instant start, Instant end) {
        long seconds = Duration.between(start, end).getSeconds();
        String time = String.format(
                "%d:%02d:%02d",
                seconds / 3600,
                (seconds % 3600) / 60,
                seconds % 60);
        window.setResultTime(time);
    }

    private void onReceiveVehicle(ACLMessage rcv) {
        var name = rcv.getSender().getLocalName();
        var agentOpt = agentsContainer.get(VehicleAgent.class, (v) -> v.getLocalName().equals(name));
        if (agentOpt.isPresent()) {
            var agent = agentOpt.get();
            var vehicle = agent.getVehicle();
            if (vehicle instanceof TestCar) {
                var car = (TestCar) vehicle;
                setResultTime(car.start, car.end);
            }

            agentsContainer.remove(agent);
        }
    }

    public static Date getSimulationTime() {
        return window.getSimulationStartTime();
    }

    @Subscribe
    public void handle(PrepareSimulationEvent e) {
        logger.info("Set zone event occurred: " + e.toString());
        if (configContainer.getSimulationState() == SimulationState.READY_TO_RUN) {
            reset();
        }
        configContainer.setZone(e.zone);

        if (prepareAgents()) {
            configContainer.setSimulationState(SimulationState.READY_TO_RUN);
        }
    }


    @Subscribe
    public void handle(StartSimulationEvent e) {
        activateLightManagerAgents();
        if (configContainer.shouldGenerateCars()) {
            taskManager.scheduleCarCreation(e.carsNum, e.testCarId);
        }
    }


    public boolean prepareAgents() {
        if (configContainer.shouldGeneratePedestriansAndBuses()) {
            if (!prepareStationsAndBuses()) {
                return false;
            }
        }
        return prepareLightManagers();
    }

    private boolean prepareStationsAndBuses() {
        int busCount = 0;
        for (var busInfo : busLinesManager.getBusInfos()) {
            List<RouteNode> routeWithNodes = busInfo.getRouteInfo();
            var busLine = busInfo.getBusLine();
            for (var brigade : busInfo) {
                var brigadeNr = brigade.getBrigadeNr();
                for (Timetable timetable : brigade) {
                    boolean result = tryAddBusAgent(routeWithNodes, timetable, busLine, brigadeNr);
                    if (result) {
                        ++busCount;
                    }
                    else {
                        logger.warn("Bus agent could not be added");
                    }
                }
            }
        }

        if (busCount == 0) {
            logger.error("No buses were created");
            return false;
        }

        logger.info("Buses are created!");
        logger.info("NUMBER OF BUS AGENTS: " + busCount);
        return true;
    }

    private static boolean tryAddAgent(AbstractAgent agent) {
        try {
            container.acceptNewAgent(agent.getPredictedName(), agent);
        } catch (StaleProxyException e) {
            logger.warn("Error adding agent");
            return false;
        }

        return true;
    }

    // TODO: Delete all tryAdd... from MasterAgent to separate class: AgentCreator
    //  which will be invoked with setZone event, MasterAgent won't know anything about creation
    //  except events to which he can subscribe
    private boolean tryAddBusAgent(List<RouteNode> route, Timetable timetable, String busLine,
                                   String brigadeNr) {
        var bus = new Bus(route, timetable, busLine, brigadeNr);
        BusAgent agent = new BusAgent(idGenerator.get(BusAgent.class), bus);
        return agentsContainer.tryAdd(agent);
    }

    private boolean tryCreateLightManager(Node crossroad) {
        LightManager manager = new LightManager(idGenerator.get(LightManager.class), crossroad);
        return agentsContainer.tryAdd(manager);
    }

    private boolean tryCreateLightManager(final OSMNode centerCrossroadNode) {
        LightManager manager = new LightManager(idGenerator.get(LightManager.class), centerCrossroadNode);
        return agentsContainer.tryAdd(manager);
    }

    public static AbstractAgent tryAddNewStationAgent(OSMStation stationOSMNode) {
        StationAgent stationAgent = new StationAgent(IdGenerator.getStationAgentId(), stationOSMNode);
        osmIdToStationOSMNode.put(stationOSMNode.getId(), stationOSMNode);
        tryAddAgent(stationAgent);
        return stationAgent;
    }

    public static AbstractAgent tryAddNewPedestrianAgent(Pedestrian pedestrian) {
        PedestrianAgent pedestrianAgent = new PedestrianAgent(IdGenerator.getPedestrianId(), pedestrian);
        pedestrians.add(pedestrianAgent);
        tryAddAgent(pedestrianAgent);
        return pedestrianAgent;
    }

    // TODO: Move to agentsContainer
    public void activateLightManagerAgents() {
        agentsContainer.forEach(LightManager.class, AbstractAgent::start);
    }

    private boolean prepareLightManagers() {
        if (!configContainer.tryLockLightManagers()) {
            logger.error("Light managers are locked, cannot prepare.");
            return false;
        }

        boolean result = tryConstructLightManagers();

        configContainer.unlockLightManagers();

        if (result) {
            var lightManagers = ImmutableList.copyOf(agentsContainer.iterator(LightManager.class));
            eventBus.post(new LightManagersReadyEvent(lightManagers));
        }

        return result;
    }

    private boolean tryConstructLightManagers() {
        try {
            if (configContainer.useDeprecatedXmlForLightManagers) {
                var nodes =
                        MapAccessManager.getLightManagersNodes(configContainer.getZone());
                for (var node : nodes) {
                    if (!tryCreateLightManager(node)) {
                        return false;
                    }
                }
            }
            else {
                var lights = lightAccessManager.getLights();
                for (final OSMNode centerCrossroadNode : lights) {
                    if (centerCrossroadNode.determineParentOrientationsTowardsCrossroad()) {
                        if (!tryCreateLightManager(centerCrossroadNode)) {
                            return false;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error preparing light managers", e);
            return false;
        }

        return true;
    }

    // TODO: Still not removed from container - how to do that?
    public void reset() {
        logger.info("Resetting started");

        delete(agentsContainer.iterator(LightManager.class));
        agentsContainer.clear(LightManager.class);

        delete(agentsContainer.iterator(VehicleAgent.class));
        agentsContainer.clear(VehicleAgent.class);

        delete(agentsContainer.iterator(BusAgent.class));
        agentsContainer.clear(BusAgent.class);

        // People die last
        delete(pedestrians.iterator());
        pedestrians.clear();

        logger.info("Resetting finished");
    }

    private void delete(Iterator<? extends AbstractAgent> it) {
        try {
            it.forEachRemaining(AbstractAgent::takeDown);
        } catch (Exception e) {
            logger.warn("Failed to delete agent", e);
        }
    }
}
