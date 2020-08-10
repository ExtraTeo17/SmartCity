package smartcity;

import agents.*;
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
import org.jxmapviewer.viewer.GeoPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import osmproxy.LightAccessManager;
import osmproxy.MapAccessManager;
import osmproxy.buses.BusInfo;
import osmproxy.buses.BusLinesManager;
import osmproxy.elements.OSMNode;
import osmproxy.elements.OSMStation;
import routing.LightManagerNode;
import routing.RouteNode;
import routing.StationNode;
import smartcity.buses.Timetable;
import vehicles.MovingObjectImpl;
import vehicles.Pedestrian;
import vehicles.TestCar;
import vehicles.TestPedestrian;
import web.IWebService;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

// TODO: This class should have no more than 10 fields.
// TODO: This class should be package private
public class MasterAgent extends Agent {
    private static final Logger logger = LoggerFactory.getLogger(MasterAgent.class);

    private static AgentContainer container;
    private static MapWindow window;
    private final IWebService webService;
    private final BusLinesManager busLinesManager;

    public final static boolean USE_DEPRECATED_XML_FOR_LIGHT_MANAGERS = false;
    public final static String STEPS = "6";
    public static boolean SHOULD_GENERATE_PEDESTRIANS_AND_BUSES = true;
    public static boolean SHOULD_GENERATE_CARS = false;

    // TODO: Delete this abomination (or at least make it private)
    public static boolean lightManagersUnderConstruction = false;
    public static final Set<LightManager> lightManagers = ConcurrentHashMap.newKeySet();
    public static final List<PedestrianAgent> pedestrians = new ArrayList<>();
    public static final Set<BusAgent> buses = new LinkedHashSet<>();
    public static final List<VehicleAgent> vehicles = new ArrayList<>();

    public static Map<Pair<Long, Long>, LightManagerNode> wayIdLightIdToLightManagerNode = new HashMap<>();
    public static Map<Long, LightManagerNode> crossingOsmIdToLightManagerNode = new HashMap<>();
    public static Map<Long, StationNode> osmStationIdToStationNode = new HashMap<>();
    public static Map<Long, OSMStation> osmIdToStationOSMNode = new HashMap<>();

    public int carId = 0;
    public int pedestrianId = 0;

    @Inject
    public MasterAgent(IWebService webService,
                       BusLinesManager busLinesManager,
                       MapWindow window) {
        this.webService = webService;
        this.busLinesManager = busLinesManager;

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
                        case MessageParameter.BUS -> buses.removeIf(v -> v.getLocalName().equals(rcv.getSender().getLocalName()));
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
        for (int i = 0; i < vehicles.size(); i++) {
            VehicleAgent v = vehicles.get(i);
            if (v.getLocalName().equals(rcv.getSender().getLocalName())) {
                if (v.getVehicle() instanceof TestCar) {
                    TestCar car = (TestCar) v.getVehicle();
                    setResultTime(car.start, car.end);
                }
                vehicles.remove(i);
                break;
            }
        }
    }

    public static Date getSimulationTime() {
        return window.getSimulationStartTime();
    }

    @Subscribe
    public void handleSimulationReady(SimulationReadyEvent e) {
        logger.info("Handling SimulationReadyEvent");
        var positions = lightManagers.stream()
                .flatMap(man -> man.getLightsPositions().stream())
                .collect(Collectors.toList());
        webService.setZone(positions);
    }

    public boolean prepareAgents(double lat, double lon, int radius) {
        var zoneCenter = new GeoPosition(lat, lon);
        if (SHOULD_GENERATE_PEDESTRIANS_AND_BUSES) {
            if (!prepareStationsAndBuses(zoneCenter, radius)) {
                return false;
            }
        }
        return prepareLightManagers(zoneCenter, radius);
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

    public static void tryAddNewBusAgent(final Timetable timetable, List<RouteNode> route,
                                         final String busLine, final String brigadeNr) {
        BusAgent agent = new BusAgent(IdGenerator.getBusId(), route, timetable, busLine, brigadeNr);
        buses.add(agent);
        tryAddAgent(agent);
    }

    public static void tryCreateLightManager(Node crossroad) {
        LightManager manager = new LightManager(crossroad, IdGenerator.getLightManagerId());
        lightManagers.add(manager);
        if (!tryAddAgent(manager)) {
            logger.warn("Failed to add LightManager.");
        }
    }

    public static void tryCreateLightManager(final OSMNode centerCrossroadNode) {
        LightManager manager = new LightManager(centerCrossroadNode, IdGenerator.getLightManagerId());
        lightManagers.add(manager);
        tryAddAgent(manager);
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

    public VehicleAgent tryAddNewVehicleAgent(List<RouteNode> info, boolean testCar) {
        VehicleAgent vehicle = new VehicleAgent(carId);
        MovingObjectImpl car = testCar ? new TestCar(info) : new MovingObjectImpl(info);
        vehicle.setVehicle(car);

        tryAddNewVehicleAgent(vehicle);

        return vehicle;
    }

    public VehicleAgent tryAddNewVehicleAgent(List<RouteNode> info) {
        return tryAddNewVehicleAgent(info, false);
    }

    private void tryAddNewVehicleAgent(VehicleAgent agent) {
        tryAddAgent(agent);
        vehicles.add(agent);
        ++carId;
    }

    public void activateLightManagerAgents() {
        for (LightManager lightManager : lightManagers) {
            lightManager.start();
        }
    }

    public boolean prepareLightManagers(GeoPosition middlePoint, int radius) {
        IdGenerator.resetLightManagerId();
        lightManagersUnderConstruction = true;
        boolean result;
        if (USE_DEPRECATED_XML_FOR_LIGHT_MANAGERS) {
            result = MapAccessManager.prepareLightManagersInRadiusAndLightIdToLightManagerIdHashSet(middlePoint, radius);
        }
        else {
            result = tryConstructLightManagers(middlePoint, radius);
        }
        lightManagersUnderConstruction = false;

        return result;
    }

    private boolean tryConstructLightManagers(GeoPosition middlePoint, int radius) {
        try {
            LightAccessManager.constructLightManagers(middlePoint, radius);
        } catch (Exception e) {
            logger.error("Error preparing light managers", e);
            return false;
        }

        return true;
    }

    public boolean prepareStationsAndBuses(GeoPosition middlePoint, int radius) {
        IdGenerator.resetStationAgentId();
        logger.info("STEP 1/" + STEPS + ": Starting bus preparation");
        IdGenerator.resetBusId();
        buses.clear();

        Set<BusInfo> busInfoSet = null;
        try {
            busInfoSet = busLinesManager.getBusInfo(radius, middlePoint.getLatitude(),
                    middlePoint.getLongitude());
        } catch (Exception e) {
            return false;
        }

        logger.info("STEP 5/" + STEPS + ": Starting agent preparation based on queries");
        int i = 0;
        for (BusInfo info : busInfoSet) {
            logger.info("STEP 5/" + STEPS + " (SUBSTEP " + (++i) + "/" + busInfoSet.size() + "): Agent preparation substep");
            info.prepareAgents();
        }
        logger.info("STEP 6/" + STEPS + ": Buses are created!");
        logger.info("NUMBER OF BUS AGENTS: " + buses.size());

        return true;
    }

    public static void reset() {
        IdGenerator.resetLightManagerId();
        for (var manager : lightManagers) {
            manager.takeDown();
        }
        lightManagers.clear();

        for (var vehicle : vehicles) {
            vehicle.takeDown();
        }
        vehicles.clear();

        for (var bus : buses) {
            bus.takeDown();
        }
        buses.clear();

        // People die last
        for (var pedestrian : pedestrians) {
            pedestrian.takeDown();
        }
        pedestrians.clear();

        // TODO: Still not removed from container - how to do that?
    }


}
