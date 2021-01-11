package osmproxy;

import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Node;

import osmproxy.elements.OSMWay;

public class WayWithLights {

	public Node way;
	public Set<Node> lightIds = new HashSet<>();
	
	public void addWay(Node osmWay) {
		way = osmWay;
	}

	public void add(Node id) {
		lightIds.add(id);
	}
}
