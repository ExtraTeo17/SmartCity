package osmproxy.buses;

import org.jetbrains.annotations.NotNull;
import osmproxy.elements.OSMStation;
import osmproxy.elements.OSMWay;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class BusInfo implements
        Iterable<BrigadeInfo>, Serializable {
    public final String busLine;
    public final List<OSMWay> route;
    public final List<OSMStation> stops;

    public final List<BrigadeInfo> brigadeList;

    BusInfo(String busLine, List<OSMWay> route) {
        this.busLine = busLine;
        this.route = route;
        this.stops = new ArrayList<>();
        this.brigadeList = new ArrayList<>();
    }

    void addStops(Collection<OSMStation> stops) {
        this.stops.addAll(stops);
    }

    void addBrigades(Collection<BrigadeInfo> brigades) {
        brigadeList.addAll(brigades);
    }

    @NotNull
    @Override
    public Iterator<BrigadeInfo> iterator() {
        return brigadeList.iterator();
    }

    @Override
    public String toString() {
        return "#########################################\n" +
                "Bus line: " + busLine + "\n" +
                "Route: " + getRouteString() + "\n" +
                "Stops: " + getStopsString() + "\n" +
                "#########################################";
    }

    private String getRouteString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[:");
        for (int i = 0; i < route.size(); ++i) {
            builder.append("route[").append(i).append("]=").append(route.get(i).getId()).append(":");
        }
        builder.append("]");
        return builder.toString();
    }

    private String getStopsString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[:");
        for (int i = 0; i < stops.size(); ++i) {
            builder.append("stops[").append(i).append("]=").append(stops.get(i).getBusStopId()).append("/").append(stops.get(i).getBusStopNr()).append("/").append(stops.get(i).getId()).append(":");
        }
        builder.append("]");
        return builder.toString();
    }
}
