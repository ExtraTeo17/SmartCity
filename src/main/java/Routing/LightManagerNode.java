package Routing;

public class LightManagerNode extends RouteNode {

	private long lightManagerId;
	
	public LightManagerNode(double lat, double lon, long osmWayId, long lightManagerId) {
		super(lat, lon, osmWayId);
		this.lightManagerId = lightManagerId;
	}

	public long getLightManagerId() {
		return lightManagerId;
	}
}
