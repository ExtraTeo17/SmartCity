package osmproxy.elements;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import routing.LightManagerNode;

import java.util.HashMap;
import java.util.Map;

public class OSMContainer {
    private final Table<Long, Long, LightManagerNode> wayIdLightIdToLightManagerNode;
    private final Map<Long, LightManagerNode> crossingIdToLightManagerNode;

    public OSMContainer() {
        wayIdLightIdToLightManagerNode = HashBasedTable.create();
        crossingIdToLightManagerNode = new HashMap<>();
    }
}
