package SmartCity;

import GUI.MapWindow;
import GUI.OSMNode;
import GUI.Router;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;

import jade.core.Agent;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import org.jxmapviewer.*;
import org.jxmapviewer.viewer.GeoPosition;

import Agents.TrafficLightAgent;

public class SmartCityAgent extends Agent {
	public int AgentCount = 0;
	private JXMapViewer mapViewer;
	private AgentContainer container;
	private MapWindow window;
	
	protected void setup() {
		container = getContainerController();
		displayGUI();
		//PointList route = getRoute(52.2301, 20.9834, 52.2296, 21.0016);
		//drawRoute(route);
		/*try {
			List<List<OSMNode>> lights = MapAccessManager.getLights(route);
		} catch (Exception e) {
			e.printStackTrace();
		} finally { }*/
		//drawLights(lights);
	}
	
	private void drawLights(List<List<OSMNode>> lights) {
		for (List<OSMNode> lightCluster : lights) {
			for (OSMNode light : lightCluster) {
				drawLight(light);
			}
		}
	}
	
	private void drawLight(OSMNode light) {
		
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

	public void AddNewAgent(String name, Agent agent) throws StaleProxyException {
		AgentController controller = container.acceptNewAgent(name, agent);
		controller.activate();
		controller.start();
		AgentCount++;
	}

	public void AddLightAgents(Router router) throws StaleProxyException {
		for (OSMNode light : router.lights) {
			String name = "Light"+light.getId();
			System.out.println(name);
			AddNewAgent(name, new TrafficLightAgent());
		}
	}
}
