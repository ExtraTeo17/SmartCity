package SmartCity;

import jade.Boot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;

import org.jxmapviewer.*;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.*;
import org.xml.sax.SAXException;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.PathWrapper;
import com.graphhopper.api.*;
import com.graphhopper.util.PointList;

public class SmartCity {
	private final static String[] defaultJadeArgs = { "-gui", "Light8:Agents.TrafficLightAgent;kotik:Agents.VehicleAgent(BasicCar);" };

	public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
		//Boot.main(parseJadeArguments(args));
		displayGUI();
		doRouting();
		MapAccessManager.traverseDatabase();
	}
	
	private final static String[] parseJadeArguments(String[] args) {
		if (args.length == 0) {
			return defaultJadeArgs;
		} else {
			return args;
		}
	}
	
	private static void doRouting() {
		GHRequest req = new GHRequest(52.2301, 20.9834, 52.2296, 21.0016).
			    setWeighting("fastest").
			    setVehicle("car").
			    setLocale(Locale.US);
		GraphHopperWeb ghweb = new GraphHopperWeb().setKey("cdd7f8b1-921e-4b98-bed2-f8f22ce919e5");
		GHResponse rsp = ghweb.route(req);
		if (rsp.hasErrors()) {
			System.out.println(rsp.getErrors());
			return;
		}
		//System.out.println(rsp);
		PathWrapper path = rsp.getBest();
		PointList points = path.getPoints();
		System.out.println("ROUTE FROM RONDO DASZYNSKIEGO TO DWORZEC CENTRALNY (D.C.):");
		System.out.println(points.toString());
		System.out.println();
	}
	
	private static void displayGUI() {
		JXMapViewer mapViewer = new JXMapViewer();
        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        tileFactory.setThreadPoolSize(8);
        mapViewer.setTileFactory(tileFactory);
        GeoPosition warsaw = new GeoPosition(52.24, 21.02);
        mapViewer.setZoom(7);
        mapViewer.setAddressLocation(warsaw);
        JFrame frame = new JFrame("Smart City by Katherine & Dominic & Robert");
        frame.getContentPane().add(mapViewer);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
	}
	
	private static void startJade(AgentData data) {
		Boot.main(data.toJadeArgs());
	}
}
