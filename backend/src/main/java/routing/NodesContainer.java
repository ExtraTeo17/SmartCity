package routing;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class NodesContainer {
    private final Table<Long, Long, LightManagerNode> wayIdLightIdToLightManagerNode;
    private final Map<Long, LightManagerNode> crossingIdToLightManagerNode;

    public NodesContainer() {
        wayIdLightIdToLightManagerNode = HashBasedTable.create();
        crossingIdToLightManagerNode = new HashMap<>();
    }

    @SuppressWarnings("FeatureEnvy")
    public void addLightManagerNode(LightManagerNode managerNode) {
        var crossingId1 = managerNode.getCrossingOsmId1();
        var crossingId2 = managerNode.getCrossingOsmId2();
        crossingIdToLightManagerNode.put(crossingId1, managerNode);
        if (crossingId2 != null) {
            crossingIdToLightManagerNode.put(crossingId2, managerNode);
        }

        var wayId = managerNode.getAdjacentWayId();
        var lightId = managerNode.getOsmLightId();
        wayIdLightIdToLightManagerNode.put(wayId, lightId, managerNode);
    }

    @Nullable
    public LightManagerNode getNode(long crossingId) {
        return crossingIdToLightManagerNode.get(crossingId);
    }

    public LightManagerNode getNode(long wayId, long lightId) {return wayIdLightIdToLightManagerNode.get(wayId, lightId);}
}
