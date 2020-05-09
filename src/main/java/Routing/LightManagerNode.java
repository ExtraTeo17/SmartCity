package Routing;

public class LightManagerNode extends RouteNode {

	public String lightManagerId;
	
	public LightManagerNode(double lat, double lon, long osmWayId, String lightManagerId) {
		super(lat, lon, osmWayId);
		this.lightManagerId = lightManagerId;
	}
}
