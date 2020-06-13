package Routing;

public class LightManagerNode extends RouteNode {

	private long lightManagerId;
	private long osmWayId;
	
	public LightManagerNode(double lat, double lon, long osmWayId, long lightManagerId) {
		super(lat, lon);
		this.lightManagerId = lightManagerId;
		this.osmWayId=osmWayId;
	}

	public long getLightManagerId() {
		return lightManagerId;
	}
	public long getOsmWayId() {
		return osmWayId;
	}
	@Override
	public boolean equals(Object obj) {
		LightManagerNode node = (LightManagerNode)obj;
		return node.getLightManagerId() == getLightManagerId() && node.getOsmWayId() == getOsmWayId();
	}
}
