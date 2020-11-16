package routing;

import com.google.common.primitives.Longs;
import routing.core.IGeoPosition;
import smartcity.lights.core.Light;

import javax.annotation.Nullable;

public class LightManagerNode extends RouteNode {
    private final int lightManagerId;
    private final long crossingOsmId1;
    private final Long crossingOsmId2;
    private final long adjacentWayId;
    private final long osmLightId;

    public LightManagerNode(IGeoPosition pos, long adjacentWayId, long adjacentCrossingOsmId1,
                            Long adjacentCrossingOsmId2, int lightManagerId, long osmLightId) {
        super(pos);
        this.lightManagerId = lightManagerId;
        this.adjacentWayId = adjacentWayId;
        this.crossingOsmId1 = adjacentCrossingOsmId1;
        this.crossingOsmId2 = adjacentCrossingOsmId2;
        this.osmLightId = osmLightId;
    }

    public int getLightManagerId() {
        return lightManagerId;
    }

    public long getAdjacentWayId() {
        return adjacentWayId;
    }

    public long getCrossingOsmId1() {
        return crossingOsmId1;
    }

    @Nullable
    Long getCrossingOsmId2() {
        return crossingOsmId2;
    }

    public long getOsmLightId() {
        return osmLightId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LightManagerNode) {
            LightManagerNode node = (LightManagerNode) obj;
            return node.getLightManagerId() == getLightManagerId() && node.getAdjacentWayId() == getAdjacentWayId();
        }

        return false;
    }

    public static LightManagerNode of(Light light, int managerId) {
        var crossingOsmId1 = light.getAdjacentCrossingOsmId1();
        var crossingOsmId2 = light.getAdjacentCrossingOsmId2();

        // Intended - first crossingId should never be null - method annotated
        var id1 = Long.parseLong(crossingOsmId1);
        var id2 = crossingOsmId2 != null ? Longs.tryParse(crossingOsmId2) : null;

        return new LightManagerNode((IGeoPosition) light, light.getAdjacentWayId(), id1, id2, managerId,
                light.getOsmLightId());
    }
}
