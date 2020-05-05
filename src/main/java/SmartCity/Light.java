package SmartCity;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;
import org.w3c.dom.Node;

import Agents.LightColor;
import GUI.CustomWaypointRenderer;

public class Light {
	private static final String OSM_LIGHT_ID = "light";
	private static final String WAY_ID = "way";
	private static final String LAT = "lat";
	private static final String LON = "lon";
	
	private LightColor carLightColor;
	private LightColor pedestrianLightColor;
	private Queue<String> carQueue = new LinkedList<>();
	private Queue<String> pedestrianQueue = new LinkedList<>();
	private GeoPosition position;
	private long adjacentOsmWayId;
	private long osmId;
	
	public Light(Node node, LightColor color, Long managerId) { 
		this.carLightColor = color;
		osmId=Long.parseLong(node.getAttributes().getNamedItem(OSM_LIGHT_ID).getNodeValue());
		addLightOsmIdToLightIdToLightManagerIdHashSet(osmId, managerId);
		double lat = Double.parseDouble((node.getAttributes().getNamedItem(LAT).getNodeValue()));
		double lon = Double.parseDouble((node.getAttributes().getNamedItem(LON).getNodeValue()));
		position= new GeoPosition(lat,lon);
		adjacentOsmWayId = Long.parseLong((node.getAttributes().getNamedItem(WAY_ID).getNodeValue()));
	}
	
	private void addLightOsmIdToLightIdToLightManagerIdHashSet(long osmId, long managerId) {
		SmartCityAgent.lightIdToLightManagerId.put(osmId, managerId);
		// MAKE SURE THE KEY AND VALUE IS ADDED ONCE !!!
	}

	public void addCarToQueue(String carName) {
		carQueue.add(carName);
	}
	
	public String removeCarFromQueue() {
		return carQueue.remove();
	}

	public boolean isGreen() {
		return carLightColor == LightColor.GREEN;
	}

	public void draw(HashSet lightSet, WaypointPainter<Waypoint> painter) {
        lightSet.add(new DefaultWaypoint(position));
        switch (carLightColor) {
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
		if (carLightColor == LightColor.RED)
			carLightColor = LightColor.GREEN;
		else if (carLightColor == LightColor.GREEN)
			carLightColor = LightColor.RED;
	}
}