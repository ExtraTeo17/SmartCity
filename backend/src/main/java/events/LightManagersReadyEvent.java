package events;

import agents.LightManager;

import java.util.List;

public class LightManagersReadyEvent {
    public final List<LightManager> lightManagers;

    public LightManagersReadyEvent(final List<LightManager> lightManagers) {this.lightManagers = lightManagers;}
}
