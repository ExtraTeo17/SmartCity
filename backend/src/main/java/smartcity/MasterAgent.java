package smartcity;

import agents.*;
import agents.utils.MessageParameter;
import com.google.inject.Inject;
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
import osmproxy.elements.OSMNode;
import osmproxy.elements.OSMStation;
import routing.LightManagerNode;
import routing.RouteNode;
import routing.StationNode;
import smartcity.buses.BusInfo;
import smartcity.buses.Timetable;
import vehicles.MovingObjectImpl;
import vehicles.Pedestrian;
import vehicles.TestCar;
import vehicles.TestPedestrian;
import web.WebServer;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

// TODO: This class should have no more than 10 fields.
public class MasterAgent extends Agent {
    private static final Logger logger = LoggerFactory.getLogger(MasterAgent.class);

    private static AgentContainer container;
    private static MapWindow window;
    private WebServer webServer;

    public final static int SERVER_PORT = 9000;
    public final static boolean USE_DEPRECATED_XML_FOR_LIGHT_MANAGERS = false;
    public final static String STEPS = "6";
    public static boolean SHOULD_GENERATE_PEDESTRIANS_AND_BUSES = true;
    public static boolean SHOULD_GENERATE_CARS = false;

    // TODO: Delete this abomination (or at least make it private)
    public static final List<PedestrianAgent> pedestrians = new ArrayList<>();
    public static Set<LightManager> lightManagers = new HashSet<>();
    public static final Set<StationAgent> stationAgents = new HashSet<>();
    public static boolean lightManagersUnderConstruction = false;
    public static Map<Pair<Long, Long>, LightManagerNode> wayIdLightIdToLightManagerNode = new HashMap<>();
    public static Map<Long, LightManagerNode> crossingOsmIdToLightManagerNode = new HashMap<>();
    public static Map<Long, StationNode> osmStationIdToStationNode = new HashMap<>();
    public static Map<Long, OSMStation> osmIdToStationOSMNode = new HashMap<>();
    public static Set<BusAgent> buses = new LinkedHashSet<>();
    public static List<VehicleAgent> Vehicles = new ArrayList<>();
    public int carId = 0;
    public int pedestrianId = 0;

    @Inject
    public MasterAgent(WebServer webServer, MapWindow window) {
        this.webServer = webServer;
        MasterAgent.window = window;
    }

    @Override
    protected void setup() {
        container = getContainerController();
        window.setSmartCityAgent(this);
        window.display();
        addBehaviour(getReceiveMessageBehaviour());
        webServer.start();
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
        for (int i = 0; i < Vehicles.size(); i++) {
            VehicleAgent v = Vehicles.get(i);
            if (v.getLocalName().equals(rcv.getSender().getLocalName())) {
                if (v.getVehicle() instanceof TestCar) {
                    TestCar car = (TestCar) v.getVehicle();
                    setResultTime(car.start, car.end);
                }
                Vehicles.remove(i);
                break;
            }
        }
    }

    public static Date getSimulationTime() {
        return window.getSimulationStartTime();
    }

    private static void tryAddAgent(AbstractAgent agent) {
        try {
            container.acceptNewAgent(agent.getPredictedName(), agent);
        } catch (StaleProxyException e) {
            logger.warn("Error adding agent");
        }
    }

    public static void tryAddNewBusAgent(final Timetable timetable, List<RouteNode> route,
                                         final String busLine, final String brigadeNr) {
        BusAgent agent = new BusAgent(IdGenerator.getBusId(), route, timetable, busLine, brigadeNr);
        buses.add(agent);
        tryAddAgent(agent);
    }

    public static void tryAddNewLightManagerAgent(Node crossroad) {
        LightManager manager = new LightManager(crossroad, IdGenerator.getLightManagerId());
        lightManagers.add(manager);
        tryAddAgent(manager);
    }

    public static void tryAddNewLightManagerAgent(final OSMNode centerCrossroadNode) {
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
        Vehicles.add(agent);
        ++carId;
    }

    public void activateLightManagerAgents() {
        for (LightManager lightManager : lightManagers) {
            lightManager.start();
        }
    }

    public void prepareLightManagers(GeoPosition middlePoint, int radius) {
        IdGenerator.resetLightManagerId();
        lightManagersUnderConstruction = true;
        if (USE_DEPRECATED_XML_FOR_LIGHT_MANAGERS) {
            MapAccessManager.prepareLightManagersInRadiusAndLightIdToLightManagerIdHashSet(middlePoint, radius);
        }
        else {
            tryPrepareLightManagersInRadiusAndLightIdToLightManagerIdHashSetBeta(middlePoint, radius);
        }
        lightManagersUnderConstruction = false;
    }

    private void tryPrepareLightManagersInRadiusAndLightIdToLightManagerIdHashSetBeta(GeoPosition middlePoint, int radius) {
        try {
            LightAccessManager.constructLightManagers(middlePoint, radius);
        } catch (Exception e) {
            logger.error("Error preparing light managers", e);
        }
    }

    public void prepareStationsAndBuses(GeoPosition middlePoint, int radius) {
        IdGenerator.resetStationAgentId();
        logger.info("STEP 1/" + STEPS + ": Starting bus preparation");
        IdGenerator.resetBusId();
        buses = new LinkedHashSet<>();
        Set<BusInfo> busInfoSet = MapAccessManager.getBusInfo(radius, middlePoint.getLatitude(), middlePoint.getLongitude());
        logger.info("STEP 5/" + STEPS + ": Starting agent preparation based on queries");
        int i = 0;
        for (BusInfo info : busInfoSet) {
            logger.info("STEP 5/" + STEPS + " (SUBSTEP " + (++i) + "/" + busInfoSet.size() + "): Agent preparation substep");
            info.prepareAgents(container);
        }
        logger.info("STEP 6/" + STEPS + ": Buses are created!");
        logger.info("NUMBER OF BUS AGENTS: " + buses.size());
    }
}
