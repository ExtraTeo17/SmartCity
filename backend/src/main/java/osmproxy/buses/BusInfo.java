package osmproxy.buses;

import org.jetbrains.annotations.NotNull;
import osmproxy.elements.OSMStation;
import osmproxy.elements.OSMWay;
import routing.IZone;
import routing.RouteNode;
import routing.Router;
import smartcity.MasterAgent;
import smartcity.buses.BrigadeInfo;

import java.util.*;
import java.util.function.Consumer;

public class BusInfo implements Iterable<BrigadeInfo> {
    private String busLine;
    private List<OSMWay> route;
    private List<Long> stationIds;
    private List<BrigadeInfo> brigadeList = new ArrayList<>();

    BusInfo(String busLine, List<OSMWay> route, List<Long> stationIds) {
        this.busLine = busLine;
        this.route = route;
        this.stationIds = stationIds;
    }

    public String getBusLine() {
        return busLine;
    }

    public List<OSMStation> getStations() {
        List<OSMStation> stations = new ArrayList<>();
        for (long osmId : stationIds) {
            stations.add(MasterAgent.osmIdToStationOSMNode.get(osmId));
        }
        return stations;
    }

    public void setRoute(List<OSMWay> parseOsmWay) {
        route = parseOsmWay;
    }

    void setBrigadeList(Collection<BrigadeInfo> values) {
        brigadeList = new ArrayList<>(values);
    }

    public List<RouteNode> getRouteInfo() {
        return Router.generateRouteInfoForBuses(route, stationIds);
    }

    @NotNull
    @Override
    public Iterator<BrigadeInfo> iterator() {
        return brigadeList.iterator();
    }
}
