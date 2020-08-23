package osmproxy.buses;

import org.jetbrains.annotations.NotNull;
import osmproxy.elements.OSMStation;
import osmproxy.elements.OSMWay;
import routing.RouteNode;
import routing.Router;
import smartcity.MasterAgent;
import smartcity.buses.BrigadeInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class BusInfo implements Iterable<BrigadeInfo> {
    private final String busLine;
    private final List<Long> stationIds;
    private List<OSMWay> route;
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

    // TODO: as field.
    public List<RouteNode> getRouteInfo() {
        return Router.generateRouteInfoForBuses(route, stationIds);
    }

    @NotNull
    @Override
    public Iterator<BrigadeInfo> iterator() {
        return brigadeList.iterator();
    }
}
