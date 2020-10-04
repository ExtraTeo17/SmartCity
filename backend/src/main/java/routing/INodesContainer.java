package routing;

import javax.annotation.Nullable;

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
