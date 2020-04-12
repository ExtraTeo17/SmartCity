package SmartCity;

import GUI.MapWindow;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.*;

import jade.core.Agent;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import org.jxmapviewer.*;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.*;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.PathWrapper;
import com.graphhopper.api.*;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint3D;

public class SmartCityAgent extends Agent {
	public int AgentCount = 0;
	private JXMapViewer mapViewer;
	private AgentContainer container;
	private MapWindow window;

	protected void setup(){
		container = getContainerController();
		displayGUI();
		PointList route = getRoute(52.2301, 20.9834, 52.2296, 21.0016);
		drawRoute(route);

		//List<List<OSMNode>> lights = MapAccessManager.getLights(route);
		//drawLights(lights);
	}
	
	private void drawRoute(PointList route) {
		List<GeoPosition> track = new ArrayList<>();
		for (GHPoint3D point : route) {
			track.add(mapViewer.convertPointToGeoPosition(new Point2D.Double(point.lat, point.lon)));
			System.out.println(mapViewer.convertPointToGeoPosition(new Point2D.Double(point.lat, point.lon)));
		}
		drawTrack(track);
	}
	
	private void drawTrack(List<GeoPosition> track) {
        RoutePainter routePainter = new RoutePainter(track);

        // Create a compound painter that uses both the route-painter and the waypoint-painter
        List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();
        painters.add(routePainter);

        CompoundPainter<JXMapViewer> painter = new CompoundPainter<JXMapViewer>(painters);
        mapViewer.setOverlayPainter(painter);
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
	
	private PointList getRoute(double fromLat, double fromLon, double toLat, double toLon) {
		GHRequest req = new GHRequest(fromLat, fromLon, toLat, toLon).
			    setWeighting("fastest").
			    setVehicle("car").
			    setLocale(Locale.US);
		GraphHopperWeb ghweb = new GraphHopperWeb().setKey("cdd7f8b1-921e-4b98-bed2-f8f22ce919e5");
		GHResponse rsp = ghweb.route(req);
		if (rsp.hasErrors()) {
			System.out.println(rsp.getErrors());
			return null;
		}
		//System.out.println(rsp);
		PathWrapper path = rsp.getBest();
		PointList points = path.getPoints();
		System.out.println("ROUTE FROM RONDO DASZYNSKIEGO TO DWORZEC CENTRALNY (D.C.):");
		System.out.println(points.toString());
		System.out.println();
		return points;
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

}
