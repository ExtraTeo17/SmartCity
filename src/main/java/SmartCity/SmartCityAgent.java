package SmartCity;

import Agents.VehicleAgent;
import GUI.MapWindow;
import GUI.OSMNode;
import GUI.Router;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.*;

import Vehicles.Vehicle;
import jade.core.Agent;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

import org.jxmapviewer.*;

import Agents.LightManager;
import Agents.TrafficLightAgent;

public class SmartCityAgent extends Agent {
    public Set<VehicleAgent> Vehicles = new LinkedHashSet<>();
    public Set<LightManager> lightManagers = new LinkedHashSet<>();
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

    public void ActivateAgent(VehicleAgent agent) {
        try {
            agent.getContainerController().getAgent(agent.getLocalName()).start();
        } catch (ControllerException e) {
            e.printStackTrace();
        }
    }

    public void AddLightManagerAgent(String localName, LightManager manager) throws StaleProxyException {
        try {
            container.getAgent(localName);
        } catch (ControllerException e) {
            AgentController controller = container.acceptNewAgent(localName, manager);
            controller.activate();
            controller.start();
            lightManagers.add(manager);
        }
    }
}
