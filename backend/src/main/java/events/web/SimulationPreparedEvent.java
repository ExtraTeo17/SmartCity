package events.web;

import smartcity.lights.core.Light;

import java.util.List;

public class SimulationPreparedEvent {
    public final List<Light> lights;

    public SimulationPreparedEvent(List<Light> lights) {
        this.lights = lights;
    }
}
