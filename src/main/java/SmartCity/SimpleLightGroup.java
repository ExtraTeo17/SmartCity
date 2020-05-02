package SmartCity;

import java.util.HashSet;
import Agents.LightColor;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;

public class SimpleLightGroup {
	
	private Light light1;
	private Light light2;
	
	public SimpleLightGroup(LightColor color) {
		light1 = new Light(color);
		light2 = new Light(color);
	}
	
	public void drawLights(HashSet lightSet, WaypointPainter<Waypoint> painter) {
		light1.draw(lightSet, painter);
		light2.draw(lightSet, painter);
	}

	public void switchLights() {
		light1.switchLight();
		light2.switchLight();
	}
}
