package osmproxy;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SimulationData implements Serializable {

	Map<Long, WayWithLights> wayIdToWayOutput = new HashMap<>();
}
