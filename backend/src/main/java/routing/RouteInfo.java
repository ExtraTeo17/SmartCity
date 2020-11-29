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

    public final boolean hasNoWays() {
        return ways.isEmpty();
    }

    public final OSMWay getFirst() {
        return ways.get(0);
    }

    public final OSMWay getLast() {
        return ways.get(ways.size() - 1);
    }

    boolean remove(long lightOsmId) {
        return lightOsmIds.remove(lightOsmId);
    }

    public final void add(long lightOsmId) {
        lightOsmIds.add(lightOsmId);
    }

    // TODO: ways.size() == 1?
    // TODO: Add some tests for this function
    void determineRouteOrientationsAndFilterRelevantNodes(String startingOsmNodeRef, String finishingOsmNodeRef) {
        if (ways.size() == 0) {
            logger.info("No ways to determine for: " + startingOsmNodeRef + "-" + finishingOsmNodeRef);
            return;
        }
		if (ways.size() == 1) {
			logger.info("Determine route orientation for single way route info: " + startingOsmNodeRef + "-"
					+ finishingOsmNodeRef);
			ways.get(0).determineRouteOrientationAndFilterRelevantNodes(startingOsmNodeRef, finishingOsmNodeRef);
			return;
		}

        int startingNodeIndex = ways.get(0).
                determineRouteOrientationAndFilterRelevantNodes(ways.get(1), startingOsmNodeRef);
        var lastInd = ways.size() - 1;
        for (int i = 1; i < lastInd; ++i) {
            startingNodeIndex = ways.get(i)
                    .determineRouteOrientationAndFilterRelevantNodes(ways.get(i + 1), startingNodeIndex);
        }

        ways.get(lastInd).determineRouteOrientationAndFilterRelevantNodes(startingNodeIndex, finishingOsmNodeRef);
    }

    @Override
    @NotNull
    public Iterator<OSMWay> iterator() {
        return ways.iterator();
    }
}
