package GUI;

import SmartCity.MapAccessManager;
import SmartCity.SmartCityAgent;
import org.javatuples.Pair;
import org.jxmapviewer.viewer.GeoPosition;
import com.graphhopper.util.PointList;
import java.util.ArrayList;
import java.util.List;

public final class Router { // BIG REFACTOR, MOVE GETMANAGERFORLIGHTS TO MAPACCESSMANAGER AND ADD XML PARSING !!!
    public static RouteInfo generateRouteInfo(GeoPosition pointA, GeoPosition pointB) {
        Pair<List<Long>, PointList> osmWayIdsAndPointList = findRoute(pointA, pointB);
        List<OSMNode> lightsList = MapAccessManager.sendTrafficSignalOverpassQuery(osmWayIdsAndPointList.getValue0());
        List<Long> managers = getManagersForLights(lightsList);
        return new RouteInfo(osmWayIdsAndPointList.getValue1(), managers);
    }

    private static Pair<List<Long>, PointList> findRoute(GeoPosition pointA, GeoPosition pointB) {
        Pair<List<Long>, PointList> osmWayIdsAndPointList = SmartCity.HighwayAccessor.getOsmWayIdsAndPointList(new String[]{"config=config.properties", "datareader.file=mazowieckie-latest.osm.pbf"},
                pointA.getLatitude(), pointA.getLongitude(), pointB.getLatitude(), pointB.getLongitude());
        return osmWayIdsAndPointList;
    }
    
    private static List<Long> getManagersForLights(List<OSMNode> lights) {
    	List<Long> managers = new ArrayList<>();
    	for (OSMNode light : lights) {
    		managers.add(SmartCityAgent.lightIdToLightManagerId.get(light));
    	}
    	return managers;
    }
}
