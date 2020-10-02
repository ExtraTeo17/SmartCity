package events;

import smartcity.lights.core.Light;

import java.util.Collection;

public class SwitchLightsStartEvent {
    public final Collection<Light> lights;

    public SwitchLightsStartEvent(Collection<Light> lights) {
        this.lights = lights;
    }
}
