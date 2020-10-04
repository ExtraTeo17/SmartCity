package events;

import agents.LightManagerAgent;

import java.util.List;

public class LightManagersReadyEvent {
    public final List<LightManagerAgent> lightManagers;

    public LightManagersReadyEvent(final List<LightManagerAgent> lightManagers) {
        this.lightManagers = lightManagers;
    }
}
