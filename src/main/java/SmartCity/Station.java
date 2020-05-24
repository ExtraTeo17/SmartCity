package SmartCity;

import java.util.HashSet;
import java.util.Map;

import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;

import GUI.CustomWaypointRenderer;
import GUI.OSMNode;

public class Station extends OSMNode {
	
	private int stopWawId;
	private int stopWawNr;

	public Station(String id2, String latitude, String longitude, String version2, Map<String, String> tags2) {
		super(id2, latitude, longitude, version2, tags2);
		// TODO Auto-generated constructor stub
	}

	public Station(final String osmId, final String lat, final String lon, final String stationRef) {
		super(osmId, lat, lon);
		fillStopWawIdNr(stationRef);
	}

	private final void fillStopWawIdNr(String stationRef) {
		stopWawNr = Integer.parseInt(stationRef.substring(stationRef.length() - 2, stationRef.length()));
		stopWawId = Integer.parseInt(stationRef.substring(0, stationRef.length() - 2));
	}

	public int getBusStopId() {
		return stopWawId;
	}

	public int getBusStopNr() {
		return stopWawNr;
	}
	
}
