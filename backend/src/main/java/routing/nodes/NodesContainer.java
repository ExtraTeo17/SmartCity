package routing.nodes;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import routing.abstractions.INodesContainer;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class NodesContainer implements INodesContainer {
    private final Map<Long, StationNode> stationIdToStationNode;
    private final Table<Long, Long, LightManagerNode> wayIdLightIdToLightManagerNode;
    private final Map<Long, LightManagerNode> crossingIdToLightManagerNode;

    public NodesContainer() {
        stationIdToStationNode = new HashMap<>();
        wayIdLightIdToLightManagerNode = HashBasedTable.create();
        crossingIdToLightManagerNode = new HashMap<>();
    }

    @Override
    public void addStationNode(StationNode stationNode) {
        stationIdToStationNode.put(stationNode.getOsmId(), stationNode);
    }

    @Override
    @Nullable
    public StationNode getStationNode(Long id) {
        return stationIdToStationNode.get(id);
    }

    @Override
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

    @Override
    @Nullable
    public LightManagerNode getLightManagerNode(long crossingId) {
        return crossingIdToLightManagerNode.get(crossingId);
    }

    @Override
    @Nullable
    public LightManagerNode getLightManagerNode(long wayId, long lightId) {return wayIdLightIdToLightManagerNode.get(wayId, lightId);}
}
