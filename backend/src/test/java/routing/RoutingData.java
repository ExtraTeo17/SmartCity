package routing;

import routing.core.IZone;
import routing.core.Position;
import routing.core.Zone;

import java.util.ArrayList;
import java.util.stream.Stream;

public class RoutingData {
    static Stream<? extends IZone> zones() {
        var monumentToBartolomeoColleoni = Position.of(52.239390, 21.015518);
        var defaultPositionForCarZone = Position.of(52.23682, 21.01681);
        var defaultPositionForBusZOne = Position.of(52.20334, 20.86121);
        var goclaw = Position.of(52.22473, 21.09258);

        var radii = new int[]{50, 131, 133, 137, 221, 300, 400, 500, 600, 750, 1055, 2500};

        var zones = new ArrayList<Zone>();
        for (int radius : radii) {
            zones.add(Zone.of(monumentToBartolomeoColleoni, radius));
            zones.add(Zone.of(defaultPositionForCarZone, radius));
            zones.add(Zone.of(defaultPositionForBusZOne, radius));
            zones.add(Zone.of(goclaw, radius));
        }

        return zones.stream();
    }
}
