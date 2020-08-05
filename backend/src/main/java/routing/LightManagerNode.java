package routing;

public class LightManagerNode extends RouteNode {
    private final long lightManagerId;
    private final long crossingOsmId1;
    private final long crossingOsmId2;
    private long osmWayId;

    public LightManagerNode(double lat, double lon, long osmWayId, long adjacentCrossingOsmId1,
                            long adjacentCrossingOsmId2, long lightManagerId) {
        super(lat, lon);
        this.lightManagerId = lightManagerId;
        this.osmWayId = osmWayId;
        this.crossingOsmId1 = adjacentCrossingOsmId1;
        this.crossingOsmId2 = adjacentCrossingOsmId2;
    }

    public long getLightManagerId() {
        return lightManagerId;
    }

    public long getOsmWayId() {
        return osmWayId;
    }

    public void setOsmWayId(final long osmWayId) {
        this.osmWayId = osmWayId;
    }

    public long getCrossingOsmId1() {
        return crossingOsmId1;
    }

    public long getCrossingOsmId2() {
        return crossingOsmId2;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LightManagerNode) {
            LightManagerNode node = (LightManagerNode) obj;
            return node.getLightManagerId() == getLightManagerId() && node.getOsmWayId() == getOsmWayId();
        }

        return false;
    }
}
