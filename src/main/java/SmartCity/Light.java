package SmartCity;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;

import Agents.LightColor;
import GUI.CustomWaypointRenderer;

public class Light {
	
	private LightColor color;
	private Queue<String> carQueue = new LinkedList<>();
	private GeoPosition position;
	
	public Light(LightColor color) { // TODO: ADD GEOPOSITION !!!
		this.color = color;
	}

	public void addCarToQueue(String carName) {
		carQueue.add(carName);
	}
	
	public String removeCarFromQueue() {
		return carQueue.remove();
	}

	public boolean isGreen() {
		return color == LightColor.GREEN;
	}

	public void draw(HashSet lightSet, WaypointPainter<Waypoint> painter) {
        lightSet.add(new DefaultWaypoint(position));
        switch (color) {
            case RED:
                painter.setRenderer(new CustomWaypointRenderer("light_red.png"));
                break;
            case YELLOW:
                painter.setRenderer(new CustomWaypointRenderer("light_yellow.png"));
                break;
            case GREEN:
                painter.setRenderer(new CustomWaypointRenderer("light_green.png"));
                break;
        }
	}

	public void switchLight() {
		if (color == LightColor.RED)
			color = LightColor.GREEN;
		else if (color == LightColor.GREEN)
			color = LightColor.RED;
	}
}
