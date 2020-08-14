package osmproxy.buses;

import org.jetbrains.annotations.NotNull;
import osmproxy.MapAccessManager;
import osmproxy.elements.OSMStation;
import osmproxy.elements.OSMWay;
import routing.RouteNode;
import routing.Router;
import smartcity.MasterAgent;
import smartcity.buses.BrigadeInfo;
import utilities.NumericHelper;
import utilities.Point;

import java.util.*;
import java.util.function.Consumer;

public class BusInfo implements Iterable<BrigadeInfo> {
    private String busLine;
    private List<OSMWay> route = new ArrayList<>();
    private List<BrigadeInfo> brigadeList = new ArrayList<>();
    private List<Long> stationsOnRouteOsmIds = new ArrayList<>();

    public String getBusLine() {
        return busLine;
    }

    public void setBusLine(String nodeValue) {
        busLine = nodeValue;
    }

    public void addStation(String nodeValue) {
        stationsOnRouteOsmIds.add(Long.parseLong(nodeValue));
    }

    public List<OSMStation> getStations() {
        List<OSMStation> stations = new ArrayList<>();
        for (long osmId : stationsOnRouteOsmIds) {
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
        return Router.generateRouteInfoForBuses(route, stationsOnRouteOsmIds);
    }

    public void filterStationsByCircle(double middleLat, double middleLon, int radius) {
        List<Long> filteredStationOsmIds = new ArrayList<>();
        for (Long osmStationId : stationsOnRouteOsmIds) {
            OSMStation station = MasterAgent.osmIdToStationOSMNode.get(osmStationId);
            if (station != null && NumericHelper.belongsToCircle(Point.of(station.getLat(), station.getLon()),
                    Point.of(middleLat, middleLon), radius / MapAccessManager.METERS_PER_DEGREE)) {
                filteredStationOsmIds.add(osmStationId);
            }
        }
        stationsOnRouteOsmIds = filteredStationOsmIds;
    }

    @NotNull
    @Override
    public Iterator<BrigadeInfo> iterator() {
        return brigadeList.iterator();
    }

    @Override
    public void forEach(Consumer<? super BrigadeInfo> action) {
        brigadeList.forEach(action);
    }

    @Override
    public Spliterator<BrigadeInfo> spliterator() {
        return brigadeList.spliterator();
    }
}
