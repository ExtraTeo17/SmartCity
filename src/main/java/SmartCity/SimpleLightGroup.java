package SmartCity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import Agents.LightColor;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SimpleLightGroup {
	
	private Set<Light> lights;
	
	public SimpleLightGroup(Node crossroadGroup, LightColor color, Long managerId) {
		lights = new HashSet<>();
		NodeList lightsInGroup = crossroadGroup.getChildNodes();
		for (int i = 0; i < lightsInGroup.getLength(); ++i) {
			if (lightsInGroup.item(i).getNodeName().equals("light"))
				lights.add(new Light(lightsInGroup.item(i), color, managerId));
		}
	}
	
	public void drawLights( WaypointPainter<Waypoint> painter) {
		HashSet<Waypoint> set = new HashSet<>();
		
		for (Light light : lights) {
		
			light.draw(set, painter);
		}
		painter.setWaypoints(set);
	}

	public void switchLights() {
		for (Light light : lights) {
			light.switchLight();
		}
	}

	public Map<? extends Long, ? extends Light> prepareMap() {
		Map<Long, Light> lightMap=new HashMap<>();
		for(Light light : lights) {
			lightMap.put(light.getAdjacentOSMWayId(),light);
		}
		return lightMap;
	}
}
