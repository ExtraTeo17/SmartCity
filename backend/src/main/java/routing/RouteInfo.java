package routing;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import osmproxy.elements.OSMWay;

import java.util.*;

public class RouteInfo implements Iterable<OSMWay> {
    private static final Logger logger = LoggerFactory.getLogger(RouteInfo.class);
    private final List<OSMWay> ways = new ArrayList<>();
    private final Set<Long> lightOsmIds = new HashSet<>();

    public final void addWay(final OSMWay way) {
        ways.add(way);
    }

    boolean remove(final String lightOsmIdString) {
        final long lightOsmId = Long.parseLong(lightOsmIdString);
        if (lightOsmIds.contains(lightOsmId)) {
            lightOsmIds.remove(lightOsmId);
            return true;
        }
        return false;
    }

    public final void add(final String lightOsmId) {
        lightOsmIds.add(Long.parseLong(lightOsmId));
    }

    // TODO: ways.size() == 1?
    // TODO: Add some tests for this function
    void determineRouteOrientationsAndFilterRelevantNodes(String startingOsmNodeRef, String finishingOsmNodeRef) {
        if (ways.size() == 0 || ways.size() == 1) {
            logger.warn("No ways to determine");
            return;
        }

        int startingNodeIndex = ways.get(0).
                determineRouteOrientationAndFilterRelevantNodes(ways.get(1), startingOsmNodeRef);
        var lastInd = ways.size() - 1;
        for (int i = 1; i < lastInd; ++i) {
            startingNodeIndex = ways.get(i).determineRouteOrientationAndFilterRelevantNodes(ways.get(i + 1), startingNodeIndex);
        }

        ways.get(lastInd).determineRouteOrientationAndFilterRelevantNodes(startingNodeIndex, finishingOsmNodeRef);
    }

    @Override
    @NotNull
    public Iterator<OSMWay> iterator() {
        return ways.iterator();
    }
}
