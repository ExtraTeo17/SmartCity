package osmproxy.buses;

import org.jetbrains.annotations.NotNull;
import osmproxy.elements.OSMStation;
import osmproxy.elements.OSMWay;
import routing.RouteNode;
import routing.Router;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class BusInfo implements Iterable<BrigadeInfo> {
    private final String busLine;
    private final List<OSMWay> route;
    private List<OSMStation> stops;
    private List<BrigadeInfo> brigadeList;
    private List<RouteNode> routeInfo;

    BusInfo(String busLine, List<OSMWay> route) {
        this.busLine = busLine;
        this.route = route;
        this.stops = new ArrayList<>();
        this.brigadeList = new ArrayList<>();
    }

    public String getBusLine() {
        return busLine;
    }

    List<OSMStation> getStops() {
        return stops;
    }

    void setStops(Collection<OSMStation> stops) {
        this.stops = new ArrayList<>(stops);
    }

    void setBrigadeList(Collection<BrigadeInfo> values) {
        brigadeList = new ArrayList<>(values);
    }

    // TODO: Remove routeInfo from here
    public List<RouteNode> generateRouteInfo() {
        if (routeInfo == null) {
            routeInfo = Router.generateRouteInfoForBuses(route, stops);
        }
        return routeInfo;
    }

    @NotNull
    @Override
    public Iterator<BrigadeInfo> iterator() {
        return brigadeList.iterator();
    }
}
