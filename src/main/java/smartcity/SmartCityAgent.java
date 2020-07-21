package smartcity;

import agents.*;
import gui.MapWindow;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;
import jade.wrapper.ControllerException;
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
import web.WebServerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

// TODO: This class should have no more than 10 fields.
public class SmartCityAgent extends Agent {
    private static final Logger logger = LoggerFactory.getLogger(SmartCityAgent.class);
    private static long nextLightManagerId;
    private static long nextStationAgentId;
    private static int nextBusId;
    private static int nextPedestrianAgentId;
    private static AgentContainer container;
    private static MapWindow window;
    private static WebServer webServer;

    public final static int SERVER_PORT = 9000;
    public final static String LIGHT_MANAGER = "LightManager";
    public final static String BUS = "Bus";
    public final static String STATION = "Station";
    public final static String PEDESTRIAN = "Pedestrian";
    public final static boolean USE_DEPRECATED_XML_FOR_LIGHT_MANAGERS = false;
    public final static String STEPS = "6";
    public static boolean SHOULD_GENERATE_PEDESTRIANS_AND_BUSES = false;
    public static boolean SHOULD_GENERATE_CARS = true;

    // TODO: Delete this abomination (or at least make it private)
    public static final List<PedestrianAgent> pedestrians = new ArrayList<>();
    public static Set<LightManager> lightManagers = new HashSet<>();
    public static Set<StationAgent> stationAgents = new HashSet<>();
    public static boolean lightManagersUnderConstruction = false;
    public static Map<Pair<Long, Long>, LightManagerNode> wayIdLightIdToLightManagerNode = new HashMap<>();
    public static Map<Long, LightManagerNode> crossingOsmIdToLightManagerNode = new HashMap<>();
    public static Map<Long, StationNode> osmStationIdToStationNode = new HashMap<>();
    public static Map<Long, OSMStation> osmIdToStationOSMNode = new HashMap<>();
    public static Set<BusAgent> buses = new LinkedHashSet<>();
    public static List<VehicleAgent> Vehicles = new ArrayList<>();
    public int carId = 0;
    public int pedestrianId = 0;

    @Override
    protected void setup() {
        SmartCityAgent.container = getContainerController();
        SmartCityAgent.window = WindowInitializer.displayWindow(this);
        addBehaviour(getReceiveMessageBehaviour());

        SmartCityAgent.webServer = WebServerFactory.Create(SmartCityAgent.SERVER_PORT);
        SmartCityAgent.webServer.start();
    }

    private CyclicBehaviour getReceiveMessageBehaviour() {
        return new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage rcv = receive();
                if (rcv != null) {
                    SmartCityAgent.logger.info("SmartCity: " + rcv.getSender().getLocalName() + " arrived at destination."); // TODO: Does it work?? (can't see it in the logs)
                    String type = rcv.getUserDefinedParameter(MessageParameter.TYPE);
                    switch (type) {
                        case MessageParameter.VEHICLE -> onReceiveVehicle(rcv);
                        case MessageParameter.PEDESTRIAN -> onReceivePedestrian(rcv);
                        case MessageParameter.BUS -> SmartCityAgent.buses.removeIf(v -> v.getLocalName().equals(rcv.getSender().getLocalName()));
                    }
                }
                block(1000);
            }
        };
    }

    private void onReceivePedestrian(ACLMessage rcv) {
        for (int i = 0; i < SmartCityAgent.pedestrians.size(); i++) {
            PedestrianAgent v = SmartCityAgent.pedestrians.get(i);
            if (v.getLocalName().equals(rcv.getSender().getLocalName())) {
                if (v.getPedestrian() instanceof TestPedestrian) {
                    TestPedestrian pedestrian = (TestPedestrian) v.getPedestrian();
                    setResultTime(pedestrian.start, pedestrian.end);
                }
                SmartCityAgent.pedestrians.remove(i);
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
        SmartCityAgent.window.setResultTime(time);
    }

    private void onReceiveVehicle(ACLMessage rcv) {
        for (int i = 0; i < SmartCityAgent.Vehicles.size(); i++) {
            VehicleAgent v = SmartCityAgent.Vehicles.get(i);
            if (v.getLocalName().equals(rcv.getSender().getLocalName())) {
                if (v.getVehicle() instanceof TestCar) {
                    TestCar car = (TestCar) v.getVehicle();
                    setResultTime(car.start, car.end);
                }
                SmartCityAgent.Vehicles.remove(i);
                break;
            }
        }
    }

    public static Date getSimulationTime() {
        return SmartCityAgent.window.getSimulationStartTime();
    }

    public static void ActivateAgent(Agent agent) {
        try {
            agent.getContainerController().getAgent(agent.getLocalName()).start();
        } catch (ControllerException e) {
            e.printStackTrace();
        }
    }

    private static long nextLightManagerId() {
        return SmartCityAgent.nextLightManagerId++;
    }

    private static long nextStationAgentId() {
        return SmartCityAgent.nextStationAgentId++;
    }

    private static int nextBusId() {
        return SmartCityAgent.nextBusId++;
    }

    private static int nextPedestrianAgentId() {
        return SmartCityAgent.nextPedestrianAgentId++;
    }

    private static void tryAddAgent(Agent agent, String agentName) {
        try {
            SmartCityAgent.container.acceptNewAgent(agentName, agent);
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

    public static void tryAddNewBusAgent(final Timetable timetable, List<RouteNode> route,
                                         final String busLine, final String brigadeNr) {
        BusAgent agent = new BusAgent();
        final int busAgentId = SmartCityAgent.nextBusId();
        agent.setArguments(new Object[]{route, timetable, busLine, brigadeNr, busAgentId});
        SmartCityAgent.buses.add(agent);
        SmartCityAgent.tryAddAgent(agent, SmartCityAgent.BUS + busAgentId);
    }

    public static void tryAddNewLightManagerAgent(Node crossroad) {
        LightManager manager = new LightManager(crossroad, SmartCityAgent.nextLightManagerId());
        SmartCityAgent.lightManagers.add(manager);
        SmartCityAgent.tryAddAgent(manager, SmartCityAgent.LIGHT_MANAGER + manager.getId());
    }

    public static void tryAddNewLightManagerAgent(final OSMNode centerCrossroadNode) {
        LightManager manager = new LightManager(centerCrossroadNode, SmartCityAgent.nextLightManagerId());
        SmartCityAgent.lightManagers.add(manager);
        SmartCityAgent.tryAddAgent(manager, SmartCityAgent.LIGHT_MANAGER + manager.getId());
    }

    public static Agent tryAddNewStationAgent(OSMStation stationOSMNode) {
        StationAgent stationAgent = new StationAgent(stationOSMNode, SmartCityAgent.nextStationAgentId());
        SmartCityAgent.osmIdToStationOSMNode.put(stationOSMNode.getId(), stationOSMNode);
        SmartCityAgent.tryAddAgent(stationAgent, SmartCityAgent.STATION + stationAgent.getAgentId());
        return stationAgent;
    }

    public static Agent tryAddNewPedestrianAgent(Pedestrian pedestrian) {
        PedestrianAgent pedestrianAgent = new PedestrianAgent(pedestrian, SmartCityAgent.nextPedestrianAgentId());
        SmartCityAgent.pedestrians.add(pedestrianAgent);
        SmartCityAgent.tryAddAgent(pedestrianAgent, SmartCityAgent.PEDESTRIAN + pedestrianAgent.getAgentId());
        return pedestrianAgent;
    }


    protected void addNewCarAgent(List<RouteNode> info) {
        VehicleAgent vehicle = new VehicleAgent();
        MovingObjectImpl car = new MovingObjectImpl(info);
        vehicle.setVehicle(car);
        try {
            addNewVehicleAgent(car.getVehicleType() + carId, vehicle);
            ++carId;
        } catch (StaleProxyException ex) {
            ex.printStackTrace();
        }
    }

    public void addNewVehicleAgent(String name, VehicleAgent agent) throws StaleProxyException {
        SmartCityAgent.container.acceptNewAgent(name, agent);
        SmartCityAgent.Vehicles.add(agent);
    }

    public void activateLightManagerAgents() {
        for (LightManager lightManager : SmartCityAgent.lightManagers) {
            SmartCityAgent.ActivateAgent(lightManager);
        }
    }

    public void prepareLightManagers(GeoPosition middlePoint, int radius) {
        resetIdGenerator();
        SmartCityAgent.lightManagersUnderConstruction = true;
        if (SmartCityAgent.USE_DEPRECATED_XML_FOR_LIGHT_MANAGERS) {
            MapAccessManager.prepareLightManagersInRadiusAndLightIdToLightManagerIdHashSet(this, middlePoint, radius);
        }
        else {
            tryPrepareLightManagersInRadiusAndLightIdToLightManagerIdHashSetBeta(this, middlePoint, radius);
        }
        SmartCityAgent.lightManagersUnderConstruction = false;
    }

    private void tryPrepareLightManagersInRadiusAndLightIdToLightManagerIdHashSetBeta(SmartCityAgent smartCityAgent,
                                                                                      GeoPosition middlePoint, int radius) {
        try {
            LightAccessManager.prepareLightManagersInRadiusAndLightIdToLightManagerIdHashSetBeta(this, middlePoint, radius);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void resetIdGenerator() {
        SmartCityAgent.nextLightManagerId = 1;
    }

    private void resetBusIdGen() {
        SmartCityAgent.nextBusId = 1;
    }

    private void resetStationAgentIdGenerator() {
        SmartCityAgent.nextStationAgentId = 1;
    }

    private void resetPedestrianAgentIdGenerator() {
        SmartCityAgent.nextPedestrianAgentId = 1;
    }

    public void prepareStationsAndBuses(GeoPosition middlePoint, int radius) {
        resetStationAgentIdGenerator();
        SmartCityAgent.logger.info("STEP 1/" + SmartCityAgent.STEPS + ": Starting bus preparation");
        resetBusIdGen();
        SmartCityAgent.buses = new LinkedHashSet<>();
        Set<BusInfo> busInfoSet = MapAccessManager.getBusInfo(radius, middlePoint.getLatitude(), middlePoint.getLongitude());
        SmartCityAgent.logger.info("STEP 5/" + SmartCityAgent.STEPS + ": Starting agent preparation based on queries");
        int i = 0;
        for (BusInfo info : busInfoSet) {
            SmartCityAgent.logger.info("STEP 5/" + SmartCityAgent.STEPS + " (SUBSTEP " + (++i) + "/" + busInfoSet.size() + "): Agent preparation substep");
            info.prepareAgents(SmartCityAgent.container);
        }
        SmartCityAgent.logger.info("STEP 6/" + SmartCityAgent.STEPS + ": Buses are created!");
        SmartCityAgent.logger.info("NUMBER OF BUS AGENTS: " + SmartCityAgent.buses.size());
    }
}
