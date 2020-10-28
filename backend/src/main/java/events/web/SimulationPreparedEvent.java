package events.web;

import osmproxy.elements.OSMStation;
import smartcity.lights.core.Light;
import vehicles.Bus;

import java.util.List;

public class SimulationPreparedEvent {
    public final List<Light> lights;
    public final List<OSMStation> stations;
    public final List<Bus> buses;

    public SimulationPreparedEvent(List<Light> lights, List<OSMStation> stations, List<Bus> buses) {
        this.lights = lights;
        this.stations = stations;
        this.buses = buses;
    }
}
