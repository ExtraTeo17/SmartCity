package smartcity.lights.core;


import routing.core.IGeoPosition;

public class LightInfo {
    final long osmLightId;
    final long adjacentOsmWayId;
    final IGeoPosition position;
    final String adjacentCrossingOsmId1;
    final String adjacentCrossingOsmId2;

    public LightInfo(long osmLightId,
                     long adjacentOsmWayId,
                     IGeoPosition position,
                     String adjacentCrossingOsmId1,
                     String adjacentCrossingOsmId2) {
        this.osmLightId = osmLightId;
        this.adjacentOsmWayId = adjacentOsmWayId;
        this.position = position;
        this.adjacentCrossingOsmId1 = adjacentCrossingOsmId1;
        this.adjacentCrossingOsmId2 = adjacentCrossingOsmId2;
    }
}
