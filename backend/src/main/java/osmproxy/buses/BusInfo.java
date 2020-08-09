package osmproxy.buses;

import org.jxmapviewer.viewer.GeoPosition;
import osmproxy.MapAccessManager;
import osmproxy.elements.OSMStation;
import osmproxy.elements.OSMWay;
import routing.RouteNode;
import routing.Router;
import smartcity.MasterAgent;
import smartcity.buses.BrigadeInfo;

import java.util.*;

public class BusInfo {
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

    public void prepareAgents() {
        List<RouteNode> routeWithNodes = Router.generateRouteInfoForBuses(route, stationsOnRouteOsmIds);
        for (BrigadeInfo brigade : brigadeList) {
            brigade.prepareAgents(routeWithNodes, busLine);
        }
    }

    public void filterStationsByCircle(double middleLat, double middleLon, int radius) {
        List<Long> filteredStationOsmIds = new ArrayList<>();
        for (Long osmStationId : stationsOnRouteOsmIds) {
            OSMStation station = MasterAgent.osmIdToStationOSMNode.get(osmStationId);
            if (station != null && MapAccessManager.belongsToCircle(station.getLat(), station.getLon(),
                    new GeoPosition(middleLat, middleLon), radius)) {
                filteredStationOsmIds.add(osmStationId);
            }
        }
        stationsOnRouteOsmIds = filteredStationOsmIds;
    }

}
