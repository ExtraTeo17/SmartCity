package osmproxy.buses;

import org.jetbrains.annotations.NotNull;
import osmproxy.elements.OSMStation;
import osmproxy.elements.OSMWay;
import routing.RouteNode;
import routing.Router;
import smartcity.buses.BrigadeInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class BusInfo implements Iterable<BrigadeInfo> {
    private final String busLine;
    private List<OSMStation> stops;
    private List<OSMWay> route;
    private List<BrigadeInfo> brigadeList = new ArrayList<>();

    BusInfo(String busLine, List<OSMWay> route) {
        this.busLine = busLine;
        this.route = route;
    }

    public String getBusLine() {
        return busLine;
    }

    public void setStops(Collection<OSMStation> stops) {
        this.stops = new ArrayList<>(stops);
    }

    public List<OSMStation> getStops() {
        return stops;
    }

    public void setRoute(List<OSMWay> parseOsmWay) {
        route = parseOsmWay;
    }

    void setBrigadeList(Collection<BrigadeInfo> values) {
        brigadeList = new ArrayList<>(values);
    }

    // TODO: as field.
    public List<RouteNode> getRouteInfo() {
        return Router.generateRouteInfoForBuses(route, stops);
    }

    @NotNull
    @Override
    public Iterator<BrigadeInfo> iterator() {
        return brigadeList.iterator();
    }
}
