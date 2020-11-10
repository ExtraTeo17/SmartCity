package smartcity.lights.core;

import smartcity.lights.LightColor;

import java.util.*;

class SimpleLightGroup {
    private final Set<Light> lights;

    SimpleLightGroup(List<LightInfo> infoList, LightColor color) {
        lights = new HashSet<>();
        for (LightInfo info : infoList) {
            lights.add(new Light(info, color));
        }
    }

    void switchLights() {
        for (Light light : lights) {
            light.switchLight();
        }
    }

    Map<? extends Long, ? extends Light> prepareMap() {
        Map<Long, Light> lightMap = new HashMap<>();
        for (Light light : lights) {
            lightMap.put(light.getAdjacentWayId(), light);
            // TODO: consider adding distinct structure for crossing IDs (and crossingOsmId2!!!)
            lightMap.put(Long.parseLong(light.getAdjacentCrossingOsmId1()), light);
        }
        return lightMap;
    }
}
