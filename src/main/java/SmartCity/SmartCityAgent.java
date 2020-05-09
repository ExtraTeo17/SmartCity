package SmartCity;

import Agents.VehicleAgent;
import GUI.MapWindow;
import GUI.OSMNode;
import GUI.Router;
import Routing.LightManagerNode;

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
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

import org.jxmapviewer.*;
import org.jxmapviewer.viewer.GeoPosition;
import org.w3c.dom.Node;

import Agents.LightManager;
import Agents.TrafficLightAgent;

public class SmartCityAgent extends Agent {
    public final static String LIGHT_MANAGER = "LightManager";

    public Set<VehicleAgent> Vehicles = new LinkedHashSet<>();
    //public Set<Pedestrian> pedestrians = new LinkedHashSet<>();
    public static Set<LightManager> lightManagers = new HashSet<>();
    public static boolean lightManagersUnderConstruction = false;
    public static Map<Long, LightManagerNode> lightIdToLightManagerNode = new HashMap<>();
    private static long nextLightManagerId;
    public Set<Station> stations = new LinkedHashSet<>();
    private JXMapViewer mapViewer;
    private AgentContainer container;
    private MapWindow window;

    protected void setup() {
        container = getContainerController();
        displayGUI();
    }

    private void displayGUI() {
        window = new MapWindow(this);
        mapViewer = window.MapViewer;
        JFrame frame = new JFrame("Smart City by Katherine & Dominic & Robert");
        frame.getContentPane().add(window.MainPanel);
        frame.setSize(800, 600);
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

    public void prepareStations(GeoPosition middlePoint, int radius) {
        stations = MapAccessManager.getStations(middlePoint, radius);
    }
    
    /*@Deprecated
    public void AddLightManagerAgent(String localName, LightManager manager) throws StaleProxyException {
        try {
            container.getAgent(localName);
        } catch (ControllerException e) {
            AgentController controller = container.acceptNewAgent(localName, manager);
            controller.activate();
            controller.start();
            lightManagers.add(manager);
        }
    }*/

    public void tryAddNewLightManagerAgent(Node crossroad) {
        LightManager manager = new LightManager(crossroad, nextLightManagerId());
        SmartCity.SmartCityAgent.lightManagers.add(manager);
        try {
            container.acceptNewAgent(LIGHT_MANAGER + manager.getId(), manager);
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
