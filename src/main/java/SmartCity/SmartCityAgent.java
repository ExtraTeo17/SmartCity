package SmartCity;

import Agents.*;
import GUI.MapWindow;
import GUI.Router;
import Routing.LightManagerNode;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.*;

import Routing.RouteNode;
import Vehicles.RegularCar;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

import org.jxmapviewer.*;
import org.jxmapviewer.viewer.GeoPosition;
import org.w3c.dom.Node;

public class SmartCityAgent extends Agent {
    public final static String LIGHT_MANAGER = "LightManager";

    private static final String BUS = "Bus";

    public List<VehicleAgent> Vehicles = new ArrayList<>();
    //public Set<Pedestrian> pedestrians = new LinkedHashSet<>();
    public static Set<LightManager> lightManagers = new HashSet<>();
    public static boolean lightManagersUnderConstruction = false;
    public static Map<Long, LightManagerNode> lightIdToLightManagerNode = new HashMap<>();
    private static long nextLightManagerId;
    public static Map<Long, Station> stations = new HashMap<>();
    public static Set<BusAgent> buses = new LinkedHashSet<>();
    private JXMapViewer mapViewer;
    private static AgentContainer container;
    private MapWindow window;

    public int carId = 0;

    CyclicBehaviour receiveMessage = new CyclicBehaviour() {
        @Override
        public void action() {
            ACLMessage rcv = receive();
            if (rcv != null) {
                System.out.println("SmartCity: " + rcv.getSender().getLocalName() + " arrived at destination.");
                String type = rcv.getUserDefinedParameter(MessageParameter.TYPE);
                switch (type) {
                    case MessageParameter.VEHICLE:
                        Vehicles.removeIf(v -> v.getLocalName().equals(rcv.getSender().getLocalName()));
                        break;
                }
            }
            block(1000);
        }
    };

	private static int nextBusId;

    protected void setup() {
        container = getContainerController();
        displayGUI();
        addBehaviour(receiveMessage);
    }

    private void displayGUI() {
        window = new MapWindow(this);
        mapViewer = window.MapViewer;
        JFrame frame = new JFrame("Smart City by Katherine & Dominic & Robert");
        frame.getContentPane().add(window.MainPanel);
        JMenuBar menuBar = new JMenuBar();
        JMenu view = new JMenu("View");

        final JCheckBoxMenuItem cars = new JCheckBoxMenuItem("Render cars", true);
        cars.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                window.renderCars = cars.getState();
            }
        });
        view.add(cars);

        final JCheckBoxMenuItem routes = new JCheckBoxMenuItem("Render car routes", true);
        routes.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                window.renderCarRoutes = routes.getState();
            }
        });
        view.add(routes);

        final JCheckBoxMenuItem buses = new JCheckBoxMenuItem("Render buses", true);
        cars.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                window.renderBuses = buses.getState();
            }
        });
        view.add(buses);

        final JCheckBoxMenuItem busRoutes = new JCheckBoxMenuItem("Render bus routes", true);
        routes.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                window.renderBusRoutes = busRoutes.getState();
            }
        });
        view.add(busRoutes);

        final JCheckBoxMenuItem lights = new JCheckBoxMenuItem("Render lights", true);
        lights.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                window.renderLights = lights.getState();
            }
        });
        view.add(lights);

        final JCheckBoxMenuItem zone = new JCheckBoxMenuItem("Render zone", true);
        zone.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                window.renderZone = zone.getState();
            }
        });
        view.add(zone);

        final JCheckBoxMenuItem stations = new JCheckBoxMenuItem("Render stations", true);
        stations.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                window.renderStations = stations.getState();
            }
        });
        view.add(stations);

        menuBar.add(view);

        JMenu debug = new JMenu("Debug");

        JMenuItem runTest = new JMenuItem("Test crossroad");
        runTest.addActionListener(new ActionListener() {
            private void prepareCar(List<RouteNode> info) {
                VehicleAgent vehicle = new VehicleAgent();
                RegularCar car = new RegularCar(info);
                vehicle.setVehicle(car);
                try {
                    AddNewVehicleAgent(car.getVehicleType() + carId, vehicle);
                    carId++;
                } catch (StaleProxyException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                window.setInputEnabled(false);
                double lat = 52.23702507833161;
                double lon = 21.017934679985046;
                mapViewer.setAddressLocation(new GeoPosition(lat, lon));
                mapViewer.setZoom(1);
                window.SetZone(lat, lon, 100);
                GeoPosition N = new GeoPosition(52.23758683540269, 21.017720103263855);
                GeoPosition S = new GeoPosition(52.23627934304847, 21.018092930316925);
                GeoPosition E = new GeoPosition(52.237225472020704, 21.019399166107178);
                GeoPosition W = new GeoPosition(52.23678526174392, 21.016663312911987);

                // N to S
                List<RouteNode> NS;
                try {
                    NS = Router.generateRouteInfo(N, S);

                    for (int i = 0; i < 5; i++) {
                        prepareCar(NS);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return;
                }

                // S to N
                List<RouteNode> SN;
                try {
                    SN = Router.generateRouteInfo(S, N);

                    for (int i = 0; i < 5; i++) {
                        prepareCar(SN);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return;
                }

                // E to W
                List<RouteNode> EW;
                try {
                    EW = Router.generateRouteInfo(E, W);

                    for (int i = 0; i < 5; i++) {
                        prepareCar(EW);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return;
                }

                // W to E
                List<RouteNode> WE;
                try {
                    WE = Router.generateRouteInfo(W, E);
                    for (int i = 0; i < 5; i++) {
                        prepareCar(WE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return;
                }


                activateLightManagerAgents();

                // start all
                for (VehicleAgent agent : Vehicles) {
                    ActivateAgent(agent);
                }
            }
        });

        debug.add(runTest);

        menuBar.add(debug);
        frame.setJMenuBar(menuBar);
        frame.setSize(1200, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public void AddNewVehicleAgent(String name, VehicleAgent agent) throws StaleProxyException {
        container.acceptNewAgent(name, agent);
        Vehicles.add(agent);
    }

    public void ActivateAgent(Agent agent) {
        try {
            agent.getContainerController().getAgent(agent.getLocalName()).start();
        } catch (ControllerException e) {
            e.printStackTrace();
        }
    }

    public void activateLightManagerAgents() {
        for (LightManager lightManager : lightManagers) {
            ActivateAgent(lightManager);
        }
    }

    public void prepareLightManagers(GeoPosition middlePoint, int radius) {
        resetIdGenerator();
        lightManagersUnderConstruction = true;
        MapAccessManager.prepareLightManagersInRadiusAndLightIdToLightManagerIdHashSet(this, middlePoint, radius);

        lightManagersUnderConstruction = false;
    }

    private void resetIdGenerator() {
        nextLightManagerId = 1;
    }

    private void resetBusIdGen() {
        nextBusId = 1;
    }

    private static long nextLightManagerId() {
        return nextLightManagerId++;
    }

    public void prepareStationsAndBuses(GeoPosition middlePoint, int radius) {
        //stations = MapAccessManager.getStations(middlePoint, radius);
    	resetBusIdGen();
        buses = new LinkedHashSet<>();
    	Set<BusInfo> busInfoSet = MapAccessManager.getBusInfo(radius, middlePoint.getLatitude(), middlePoint.getLongitude());
    	for (BusInfo info : busInfoSet) {
    		info.prepareAgents(container);
    	}
    }
    
    /*private Set<BusAgent> prepareBusesV0() {
    	Set<BusAgent> busSet = new LinkedHashSet<>();
    	for (Station station : stations) {
    		//busSet.addAll(getBusesIfNearby(station));
    	}
    	return busSet;
    }*/

//    private Set<BusAgent> getBusesIfNearby(Station station) {
//    	Set<BusAgent> busesOnStation = new LinkedHashSet<>();
//    	List<Integer> linesOnStation = getLinesOnStation(station.getWawId(), station.getWawNr());
//    	BusAgent busAgent = new BusAgent(nextBusId(), busNumber, timetable);
//    	tryAddAgent(busAgent);
//    	return busAgent;
//    }

	private static void tryAddAgent(Agent agent, String agentName) {
    	try {
            container.acceptNewAgent(agentName, agent);
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
    
    public static void tryAddNewBusAgent(final Timetable timetable) {
    	BusAgent agent = new BusAgent(route, timetable, nextBusId());
    	SmartCity.SmartCityAgent.buses.add(agent);
    	tryAddAgent(agent, BUS + agent.getId());
    }

    private static int nextBusId() {
		return nextBusId++;
	}

	public static void tryAddNewLightManagerAgent(Node crossroad) {
        LightManager manager = new LightManager(crossroad, nextLightManagerId());
        SmartCity.SmartCityAgent.lightManagers.add(manager);
        tryAddAgent(manager, LIGHT_MANAGER + manager.getId());
    }
}
