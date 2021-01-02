package osmproxy.elements;

import routing.nodes.StationNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class StationInfo {

    private final List<StationNode> nodes;

    public StationInfo(List<StationNode> copyOf) {
        nodes = new ArrayList<>(copyOf);
    }

    public void addAll(List<StationNode> mergedStationNodes) {
        nodes.addAll(mergedStationNodes);
    }

    public Stream<StationNode> stream() {
        return nodes.stream();
    }

}
