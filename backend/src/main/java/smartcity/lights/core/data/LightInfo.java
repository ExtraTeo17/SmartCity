package smartcity.lights.core.data;


import routing.core.IGeoPosition;

public class LightInfo {
    public final long osmLightId;
    public final long adjacentOsmWayId;
    public final IGeoPosition position;
    public final String adjacentCrossingOsmId1;
    public final String adjacentCrossingOsmId2;

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
