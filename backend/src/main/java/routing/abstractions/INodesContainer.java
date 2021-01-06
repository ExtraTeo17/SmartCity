package routing.abstractions;

import routing.nodes.LightManagerNode;
import routing.nodes.StationNode;

import javax.annotation.Nullable;

/**
 * Manage nodes (i.e. lightManagers)
 */
public interface INodesContainer {
    void addStationNode(StationNode stationNode);

    @Nullable
    StationNode getStationNode(Long id);

    void addLightManagerNode(LightManagerNode managerNode);

    @Nullable
    LightManagerNode getLightManagerNode(long crossingId);

    @Nullable
    LightManagerNode getLightManagerNode(long wayId, long lightId);
}
