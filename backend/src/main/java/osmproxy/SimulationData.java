package osmproxy;

import java.io.Serializable;
import java.util.Map;

public class SimulationData implements Serializable {

	Map<Long, WayWithLights> wayIdToWayOutput;
}
