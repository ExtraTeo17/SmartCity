package Routing;

public class LightManagerNode extends RouteNode {

	private long lightManagerId;
	private long osmWayId;
	private long crossingOsmId1;
	private long crossingOsmId2;
	
	public LightManagerNode(double lat, double lon, Long osmWayId, Long adjacentCrossingOsmId1,
			Long adjacentCrossingOsmId2, long lightManagerId) {
		super(lat, lon);
		this.lightManagerId = lightManagerId;
		this.osmWayId=osmWayId;
		this.crossingOsmId1 = crossingOsmId1;
		this.crossingOsmId2 = crossingOsmId2;
	}

	public long getLightManagerId() {
		return lightManagerId;
	}
	
	public long getOsmWayId() {
		return osmWayId;
	}
	
	public long getCrossingOsmId1() {
		return crossingOsmId1;
	}
	
	public long getCrossingOsmId2() {
		return crossingOsmId2;
	}
	
	public void setOsmWayId(final long osmWayId) {
		this.osmWayId = osmWayId;
	}
	
	@Override
	public boolean equals(Object obj) {
		LightManagerNode node = (LightManagerNode)obj;
		return node.getLightManagerId() == getLightManagerId() && node.getOsmWayId() == getOsmWayId();
	}
}
