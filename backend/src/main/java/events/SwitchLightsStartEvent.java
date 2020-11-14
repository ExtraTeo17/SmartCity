package events;

import smartcity.lights.core.Light;
import smartcity.lights.core.SimpleLightGroup;
import utilities.Siblings;

import java.util.Collection;

public class SwitchLightsStartEvent {
    public final int managerId;
    public final Siblings<SimpleLightGroup> lights;

    public SwitchLightsStartEvent(int managerId,
                                  Siblings<SimpleLightGroup> lights) {
        this.managerId = managerId;
        this.lights = lights;
    }
}
