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

	@Override
	public boolean equals(Object obj) {
		LightManagerNode node = (LightManagerNode)obj;
		return node.getLightManagerId() == getLightManagerId() && node.getOsmWayId() == getOsmWayId();
	}
}
