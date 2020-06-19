package Routing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import OSMProxy.Elements.OSMNode;
import OSMProxy.Elements.OSMWay;

public class RouteInfo {

	private List<OSMWay> ways = new ArrayList<>();
	private Set<Long> lightOsmIds = new HashSet<>();
	
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

	public void determineRouteOrientationsAndFilterRelevantNodes() {
		Integer startingNodeIndex = null;
		for (int i = 0; i < ways.size() - 1; ++i) {
			startingNodeIndex = ways.get(i).determineRouteOrientationAndFilterRelevantNodes(ways.get(i + 1), startingNodeIndex);
		}
	}
}
