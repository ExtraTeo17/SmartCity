package SmartCity;

import java.util.Map;

import OSMProxy.Elements.OSMNode;

public class StationOSMNode extends OSMNode {
	
	private int stopWawId;
	private int stopWawNr;

	public StationOSMNode(String id, String latitude, String longitude, String version2, Map<String, String> tags2) {
		super(id, latitude, longitude);
	}

	public StationOSMNode(final String osmId, final String lat, final String lon, final String stationRef) {
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
