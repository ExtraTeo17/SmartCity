package Routing;

public class StationNode extends RouteNode {

	private long stationAgentId;
	
	public StationNode(double lat, double lon,  long stationAgentId) {
		super(lat, lon);
		this.stationAgentId = stationAgentId;
	}

	public long getStationId() {
		return stationAgentId;
	}
}
