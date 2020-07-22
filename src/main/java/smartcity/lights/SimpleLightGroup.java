package smartcity.lights;

import agents.utils.LightColor;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;

public class SimpleLightGroup {

    private Set<Light> lights;

    public SimpleLightGroup(Node crossroadGroup, LightColor color, int managerId) {
        lights = new HashSet<>();
        NodeList lightsInGroup = crossroadGroup.getChildNodes();
        for (int i = 0; i < lightsInGroup.getLength(); ++i) {
            if (lightsInGroup.item(i).getNodeName().equals("light")) {
                lights.add(new Light(lightsInGroup.item(i), color, managerId));
            }
        }
    }

    public SimpleLightGroup(List<LightInfo> infoList, LightColor color, int managerId) {
        lights = new HashSet<>();
        for (LightInfo info : infoList) {
            lights.add(new Light(info, color, managerId));
        }
    }

    public void drawLights(WaypointPainter<Waypoint> painter) {
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

    public boolean areLightsGreen() {
        for (Light light : lights) {
            return light.isGreen();
        }
        throw new RuntimeException("Something is not properly initialized.");
    }

    public Map<? extends Long, ? extends Light> prepareMap() {
        Map<Long, Light> lightMap = new HashMap<>();
        for (Light light : lights) {
            lightMap.put(light.getAdjacentOSMWayId(), light);
        }
        return lightMap;
    }
}
