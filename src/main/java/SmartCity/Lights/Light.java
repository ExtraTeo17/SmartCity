package SmartCity.Lights;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.javatuples.Pair;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;
import org.w3c.dom.Node;

import Agents.LightColor;
import GUI.CustomWaypointRenderer;
import Routing.LightManagerNode;
import SmartCity.SmartCityAgent;

public class Light {
	private static final String OSM_LIGHT_ID = "light";
	private static final String WAY_ID = "way";
	private static final String LAT = "lat";
	private static final String LON = "lon";
	
	private LightColor carLightColor;
	private LightColor pedestrianLightColor;
	public Map<String, Instant> farAwayCarMap = new HashMap<>();
	public Queue<String> carQueue = new LinkedList<>();
	private Queue<String> pedestrianQueue = new LinkedList<>();
	private GeoPosition position;
	private long adjacentOsmWayId;
	private long osmId;
	
	
	public Light(Node node, LightColor color, Long managerId) { 
		this.carLightColor = color;
		osmId = Long.parseLong(node.getAttributes().getNamedItem(OSM_LIGHT_ID).getNodeValue());
		double lat = Double.parseDouble((node.getAttributes().getNamedItem(LAT).getNodeValue()));
		double lon = Double.parseDouble((node.getAttributes().getNamedItem(LON).getNodeValue()));
		position = new GeoPosition(lat, lon);
		adjacentOsmWayId = Long.parseLong((node.getAttributes().getNamedItem(WAY_ID).getNodeValue()));
		addLightOsmIdToLightIdToLightManagerIdHashSet(osmId, managerId, position, adjacentOsmWayId);
	}
	
	public Light(LightInfo info, LightColor color, Long managerId) {
		this.carLightColor = color;
		osmId = Long.parseLong(info.getOsmLightId());
		double lat = Double.parseDouble(info.getLat());
		double lon = Double.parseDouble(info.getLon());
		position = new GeoPosition(lat, lon);
		adjacentOsmWayId = Long.parseLong(info.getAdjacentOsmWayId());
		addLightOsmIdToLightIdToLightManagerIdHashSet(osmId, managerId, position, adjacentOsmWayId);
	}
	
	public long getAdjacentOSMWayId() {
		return adjacentOsmWayId;
	}
	
	private void addLightOsmIdToLightIdToLightManagerIdHashSet(long osmId, long managerId, GeoPosition pos, long osmWayId) {
		SmartCityAgent.wayIdLightIdToLightManagerNode.put(Pair.with(osmWayId, osmId),
				new LightManagerNode(pos.getLatitude(), pos.getLongitude(), osmWayId, managerId));
		
		/*int size = SmartCityAgent.lightIdToLightManagerNode.values().size();
		if (size == 5) {
			System.out.println("STAAAAAAAAAAAAAARTING PRIIIIIIIIIIIIIIINTING");
			Collection<LightManagerNode> values = SmartCityAgent.lightIdToLightManagerNode.values();
			for (LightManagerNode node : values) {
				System.out.println(node.getLightManagerId());
			}
		}*/
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

	public void addCarToFarAwayQueue(String carName, Instant journeyTime) {
		farAwayCarMap.put(carName, journeyTime);
	}

	public void addCarToQueue(String carName) {
		carQueue.add(carName);
	}

	public void removeCarFromFarAwayQueue(String carName) {
		farAwayCarMap.remove(carName);
	}
	
	public void removeCarFromQueue() {
		carQueue.remove();
	}
}
