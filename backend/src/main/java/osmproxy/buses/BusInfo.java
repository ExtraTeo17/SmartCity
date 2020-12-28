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
        StringBuilder builder = new StringBuilder();
        builder.append("#########################################\n");
        builder.append("Bus line: " + busLine + "\n");
        builder.append("Route: " + getRouteString() + "\n");
        builder.append("Stops: " + getStopsString() + "\n");
        builder.append("#########################################");
        return builder.toString();
    }

    private String getRouteString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[:");
        for (int i = 0; i < route.size(); ++i) {
            builder.append("route[" + i + "]=" + route.get(i).getId() + ":");
        }
        builder.append("]");
        return builder.toString();
    }

    private String getStopsString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[:");
        for (int i = 0; i < stops.size(); ++i) {
            builder.append("stops[" + i + "]=" + stops.get(i).getBusStopId() + "/" + stops.get(i).getBusStopNr() + "/" + stops.get(i).getId() + ":");
        }
        builder.append("]");
        return builder.toString();
    }
}
