package smartcity;

import agents.*;
import agents.abstractions.IAgentsContainer;
import agents.utils.MessageParameter;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import events.SimulationReadyEvent;
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
import smartcity.task.TaskManager;
import vehicles.*;
import web.abstractions.IWebService;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

// TODO: This class should have no more than 10 fields.
// TODO: This class should be package private
public class MasterAgent extends Agent {
    public static final String name = MasterAgent.class.getName().replace("Agent", "");
    private static final Logger logger = LoggerFactory.getLogger(MasterAgent.class);

    private static AgentContainer container;
    private static MapWindow window;
    private final IWebService webService;
    private final IBusLinesManager busLinesManager;
    private final IdGenerator<AbstractAgent> idGenerator;
    private final IAgentsContainer agentsContainer;
    private final TaskManager taskManager;
    private final LightAccessManager lightAccessManager;
    private final ConfigContainer configContainer;

    // TODO: Delete this abomination (or at least make it private)
    public static final List<PedestrianAgent> pedestrians = new CopyOnWriteArrayList<>();

    public static Map<Pair<Long, Long>, LightManagerNode> wayIdLightIdToLightManagerNode = new HashMap<>();
    public static Map<Long, LightManagerNode> crossingOsmIdToLightManagerNode = new HashMap<>();
    public static Map<Long, StationNode> osmStationIdToStationNode = new HashMap<>();
    public static Map<Long, OSMStation> osmIdToStationOSMNode = new HashMap<>();

    public int carId = 0;
    public int pedestrianId = 0;

    @Inject
    public MasterAgent(IWebService webService,
                       IBusLinesManager busLinesManager,
                       IdGenerator<AbstractAgent> idGenerator,
                       IAgentsContainer agentsContainer,
                       TaskManager taskManager,
                       LightAccessManager lightAccessManager,
                       ConfigContainer configContainer,
                       MapWindow window) {
        this.webService = webService;
        this.busLinesManager = busLinesManager;
        this.idGenerator = idGenerator;
        this.agentsContainer = agentsContainer;
        this.taskManager = taskManager;
        this.lightAccessManager = lightAccessManager;
        this.configContainer = configContainer;

        // TODO: Delete this abomination
        MasterAgent.window = window;
    }

    @Override
    protected void setup() {
        container = getContainerController();
        window.setSmartCityAgent(this);
        window.display();

        addBehaviour(getReceiveMessageBehaviour());
        webService.start();
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
    public void handleSimulationReady(SimulationReadyEvent e) {
        logger.info("Handling SimulationReadyEvent");
        var positions = agentsContainer.stream(LightManager.class)
                .flatMap(man -> man.getLightsPositions().stream())
                .collect(Collectors.toList());
        webService.setZone(positions);
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

    private boolean tryAddBusAgent(List<RouteNode> route, Timetable timetable, String busLine,
                                   String brigadeNr) {
        var bus = new Bus(route, timetable, busLine, brigadeNr);
        BusAgent agent = new BusAgent(idGenerator.get(BusAgent.class), bus);
        return agentsContainer.tryAdd(agent);
    }

    private boolean tryCreateLightManager(Node crossroad) {
        LightManager manager = new LightManager(crossroad, IdGenerator.getLightManagerId());
        return agentsContainer.tryAdd(manager);
    }

    private boolean tryCreateLightManager(final OSMNode centerCrossroadNode) {
        LightManager manager = new LightManager(centerCrossroadNode, IdGenerator.getLightManagerId());
        return agentsContainer.tryAdd(manager);
    }

    public static AbstractAgent tryAddNewStationAgent(OSMStation stationOSMNode) {
        StationAgent stationAgent = new StationAgent(stationOSMNode, IdGenerator.getStationAgentId());
        osmIdToStationOSMNode.put(stationOSMNode.getId(), stationOSMNode);
        tryAddAgent(stationAgent);
        return stationAgent;
    }

    public static AbstractAgent tryAddNewPedestrianAgent(Pedestrian pedestrian) {
        PedestrianAgent pedestrianAgent = new PedestrianAgent(pedestrian, IdGenerator.getPedestrianId());
        pedestrians.add(pedestrianAgent);
        tryAddAgent(pedestrianAgent);
        return pedestrianAgent;
    }

    public VehicleAgent tryAddNewVehicleAgent(List<RouteNode> info) {
        return tryAddNewVehicleAgent(info, false);
    }

    public VehicleAgent tryAddNewVehicleAgent(List<RouteNode> info, boolean testCar) {
        MovingObjectImpl car = testCar ? new TestCar(info) : new MovingObjectImpl(info);
        VehicleAgent agent = new VehicleAgent(carId, car);
        agentsContainer.tryAdd(agent);
        // TODO: Move id to the factory
        ++carId;

        return agent;
    }

    // TODO: Move to agentsContainer
    public void activateLightManagerAgents() {
        agentsContainer.forEach(LightManager.class, AbstractAgent::start);
    }

    private boolean prepareLightManagers() {
        IdGenerator.resetLightManagerId();
        if (!configContainer.tryLockLightManagers()) {
            logger.error("Light managers are locked, cannot prepare.");
            return false;
        }

        boolean result = tryConstructLightManagers();

        configContainer.unlockLightManagers();

        return result;
    }

    private boolean tryConstructLightManagers() {
        try {
            if (configContainer.USE_DEPRECATED_XML_FOR_LIGHT_MANAGERS) {
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
