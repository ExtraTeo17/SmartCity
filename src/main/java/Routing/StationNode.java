package Routing;

public class StationNode extends RouteNode {

	private long stationId;
	
	public StationNode(double lat, double lon, long osmWayId, long stationId) {
		super(lat, lon, osmWayId);
		this.stationId = stationId;
	}

	public long getStationId() {
		return stationId;
	}
}
