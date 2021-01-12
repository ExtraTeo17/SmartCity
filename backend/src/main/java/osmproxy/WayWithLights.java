package osmproxy;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Node;

import osmproxy.elements.OSMWay;

public class WayWithLights implements Serializable {

	public OSMWay way;
	public Set<Long> lightIds = new HashSet<>();
	
	public void addWay(OSMWay osmWay) {
		way = osmWay;
	}

	public void add(long id) {
		lightIds.add(id);
	}
}
