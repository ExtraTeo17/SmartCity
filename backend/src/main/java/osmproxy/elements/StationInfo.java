package osmproxy.elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import routing.nodes.StationNode;

public class StationInfo {
	
	private final List<StationNode> nodes;

	public StationInfo(List<StationNode> copyOf) {
		nodes = new ArrayList<StationNode>(copyOf);
	}

	public void addAll(List<StationNode> mergedStationNodes) {
		nodes.addAll(mergedStationNodes);
	}

	public Stream<StationNode> stream() {
		return nodes.stream();
	}

}
