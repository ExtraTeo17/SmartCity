package events;

import smartcity.lights.core.Light;

import java.util.Collection;

public class SwitchLightsStartEvent {
    public final int managerId;
    public final Collection<Light> lights;

    public SwitchLightsStartEvent(int managerId,
                                  Collection<Light> lights) {
        this.managerId = managerId;
        this.lights = lights;
    }
}
