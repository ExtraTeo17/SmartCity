package events.web;

import osmproxy.elements.OSMStation;
import smartcity.lights.core.Light;

import java.util.List;

public class SimulationPreparedEvent {
    public final List<Light> lights;
    public final List<OSMStation> stations;

    public SimulationPreparedEvent(List<Light> lights, List<OSMStation> stations) {
        this.lights = lights;
        this.stations = stations;
    }
}
