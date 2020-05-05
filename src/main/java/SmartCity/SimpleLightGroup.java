package SmartCity;

import java.util.HashSet;
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
			lights.add(new Light(lightsInGroup.item(i), color, managerId));
		}
	}
	
	public void drawLights(HashSet lightSet, WaypointPainter<Waypoint> painter) {
		for (Light light : lights) {
			light.draw(lightSet, painter);
		}
	}

	public void switchLights() {
		for (Light light : lights) {
			light.switchLight();
		}
	}
}
