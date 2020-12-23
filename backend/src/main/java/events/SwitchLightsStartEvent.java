package events;

import smartcity.lights.core.SimpleLightGroup;
import utilities.Siblings;

public class SwitchLightsStartEvent {
    public final int managerId;
    public final Siblings<SimpleLightGroup> lights;

    public SwitchLightsStartEvent(int managerId,
                                  Siblings<SimpleLightGroup> lights) {
        this.managerId = managerId;
        this.lights = lights;
    }
}
