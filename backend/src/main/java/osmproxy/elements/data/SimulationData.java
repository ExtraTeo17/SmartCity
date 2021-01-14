package osmproxy.elements.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SimulationData implements Serializable {

	private Map<Long, WayWithLights> wayIdToWayWithLights = new HashMap<>();

	public void put(long wayId, WayWithLights wayWithLights) {
		wayIdToWayWithLights.put(wayId, wayWithLights);
	}

	public boolean contains(long id) {
		return wayIdToWayWithLights.containsKey(id);
	}

	public WayWithLights get(long id) {
		return wayIdToWayWithLights.get(id);
	}
}
