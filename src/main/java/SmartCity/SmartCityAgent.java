package SmartCity;

import Agents.*;
import GUI.MapWindow;
import GUI.OSMNode;
import GUI.Router;
import Routing.LightManagerNode;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.*;

import Vehicles.Vehicle;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

import org.jxmapviewer.*;
import org.jxmapviewer.viewer.GeoPosition;
import org.w3c.dom.Node;

public class SmartCityAgent extends Agent {
    public final static String LIGHT_MANAGER = "LightManager";

    public List<VehicleAgent> Vehicles = new ArrayList<>();
    //public Set<Pedestrian> pedestrians = new LinkedHashSet<>();
    public static Set<LightManager> lightManagers = new HashSet<>();
    public static boolean lightManagersUnderConstruction = false;
    public static Map<Long, LightManagerNode> lightIdToLightManagerNode = new HashMap<>();
    private static long nextLightManagerId;
    public static Map<Long,Station> stations = new HashMap<>();
    public Set<BusAgent> buses = new LinkedHashSet<>();
    private JXMapViewer mapViewer;
    private AgentContainer container;
    private MapWindow window;

    public int carId = 0;

    CyclicBehaviour receiveMessage = new CyclicBehaviour() {
        @Override
        public void action() {
            ACLMessage rcv = receive();
            if(rcv != null)
            {
                System.out.println("SmartCity: " + rcv.getSender().getLocalName() + " arrived at destination.");
                String type = rcv.getUserDefinedParameter(MessageParameter.TYPE);
                switch(type)
                {
                    case MessageParameter.VEHICLE:
                        Vehicles.removeIf(v -> v.getLocalName().equals(rcv.getSender().getLocalName()));
                        break;
                }
            }
            block(1000);
        }
    };

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

    private Long nextLightManagerId() {
        return nextLightManagerId++;
    }

    public void prepareStationsAndBuses(GeoPosition middlePoint, int radius) {
        //stations = MapAccessManager.getStations(middlePoint, radius);
        prepareBuses();
    }
    
    private void prepareBuses() {
    	buses = new LinkedHashSet<>();
    	Set<BusInfo> busInfoSet = MapAccessManager.getBusInfo();
    	for (BusInfo info : busInfoSet) {
    		tryAddAgent(new BusAgent(info));
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

    
    private void tryAddAgent(Agent agent, String agentName) {
    	try {
            container.acceptNewAgent(agentName, agent);
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

    public void tryAddNewLightManagerAgent(Node crossroad) {
        LightManager manager = new LightManager(crossroad, nextLightManagerId());
        SmartCity.SmartCityAgent.lightManagers.add(manager);
        tryAddAgent(manager, LIGHT_MANAGER + manager.getId());
    }
}
