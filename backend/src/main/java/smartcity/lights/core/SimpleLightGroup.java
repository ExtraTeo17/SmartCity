package smartcity.lights.core;

import com.google.common.annotations.VisibleForTesting;
import org.jetbrains.annotations.NotNull;
import smartcity.lights.LightColor;

import java.util.*;

public class SimpleLightGroup implements Iterable<Light> {
    private final Set<Light> lights;

    SimpleLightGroup(LightColor initColor, List<LightInfo> infoList) {
        this.lights = new HashSet<>();
        for (LightInfo info : infoList) {
            lights.add(new Light(info, initColor));
        }
    }

    @VisibleForTesting
    public SimpleLightGroup(Collection<Light> lights) {
        this.lights = new HashSet<>(lights);
    }

    public void switchLights() {
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

    public Collection<? extends Light> getLights() {
        return lights;
    }

    @NotNull
    @Override
    public Iterator<Light> iterator() {
        return lights.iterator();
    }
}
