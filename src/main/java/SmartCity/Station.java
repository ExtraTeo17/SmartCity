package SmartCity;

import java.util.HashSet;
import java.util.Map;

import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;

import GUI.CustomWaypointRenderer;
import GUI.OSMNode;

public class Station extends OSMNode {

	public Station(String id2, String latitude, String longitude, String version2, Map<String, String> tags2) {
		super(id2, latitude, longitude, version2, tags2);
		// TODO Auto-generated constructor stub
	}

	public Station(String string, double lat, double lng, String nodeValue) {
		// TODO Auto-generated constructor stub
	}

	public Station(double lat, double lng, String nodeValue) {
		// TODO Auto-generated constructor stub
	}

	public int getBusStopId() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getBusStopNr() {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
