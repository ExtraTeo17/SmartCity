package routing;

import osmproxy.elements.OSMWay;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RouteInfo {
    private final List<OSMWay> ways = new ArrayList<>();
    private final Set<Long> lightOsmIds = new HashSet<>();

    public final OSMWay getWay(final int i) {
        return ways.get(i);
    }

    public final int getWayCount() {
        return ways.size();
    }

    public final void addWay(final OSMWay way) {
        ways.add(way);
    }

    public boolean removeIfContains(final String lightOsmIdString) {
        final long lightOsmId = Long.parseLong(lightOsmIdString);
        if (lightOsmIds.contains(lightOsmId)) {
            lightOsmIds.remove(lightOsmId);
            return true;
        }
        return false;
    }

    public final void addLightOsmId(final String lightOsmId) {
        lightOsmIds.add(Long.parseLong(lightOsmId));
    }

    public void determineRouteOrientationsAndFilterRelevantNodes(String startingOsmNodeRef, String finishingOsmNodeRef) {
        Integer startingNodeIndex = ways.get(0).determineRouteOrientationAndFilterRelevantNodes(ways.size() > 1 ? ways.get(1) : null, ways.get(0).indexOf(startingOsmNodeRef), null);
        for (int i = 1; i < ways.size() - 1; ++i) {
            startingNodeIndex = ways.get(i).determineRouteOrientationAndFilterRelevantNodes(ways.get(i + 1), startingNodeIndex, null);
        }
        ways.get(ways.size() - 1).determineRouteOrientationAndFilterRelevantNodes(null, startingNodeIndex, ways.get(ways.size() - 1).indexOf(finishingOsmNodeRef));
    }
}
