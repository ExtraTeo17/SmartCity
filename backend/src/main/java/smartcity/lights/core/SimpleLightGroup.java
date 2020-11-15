package smartcity.lights.core;

import com.google.common.annotations.VisibleForTesting;
import org.jetbrains.annotations.NotNull;
import smartcity.lights.LightColor;
import smartcity.lights.core.data.LightInfo;

import java.util.*;

public class SimpleLightGroup implements Iterable<Light> {
    private final Set<Light> lights;
    private final LightColor initialColor;

    SimpleLightGroup(LightColor initColor, List<LightInfo> infoList) {
        this.lights = new HashSet<>();
        for (LightInfo info : infoList) {
            lights.add(new Light(info, initColor));
        }
        initialColor = initColor;
    }

    @VisibleForTesting
    public SimpleLightGroup(Collection<Light> lights) {
        this.lights = new HashSet<>(lights);

        var light = lights.stream().findAny();
        LightColor color = LightColor.RED;
        if (light.isPresent()) {
            color = light.get().isGreen() ? LightColor.GREEN : LightColor.RED;
        }
        this.initialColor = color;
    }

    public LightColor getInitialColor() {
        return initialColor;
    }

    void switchLights() {
        for (Light light : lights) {
            light.switchLight();
        }
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
