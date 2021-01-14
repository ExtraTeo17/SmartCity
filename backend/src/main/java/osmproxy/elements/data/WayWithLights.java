package osmproxy.elements.data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Node;

import osmproxy.elements.OSMWay;

public class WayWithLights implements Serializable {

	private OSMWay way;
	private Set<Long> highwayLightIds = new HashSet<>();
	private Set<Long> crossingLightIds = new HashSet<>();
	
	public void addWay(OSMWay osmWay) {
		way = osmWay;
	}

	public void addHighwayLight(long id) {
		highwayLightIds.add(id);
	}

	public void addCrossingLight(long id) {
		crossingLightIds.add(id);
	}

	public OSMWay getWay() {
		return way;
	}

	public Set<Long> getHighwayLightIds() {
		return highwayLightIds;
	}

	public Set<Long> getCrossingLightIds() {
		return crossingLightIds;
	}
}
