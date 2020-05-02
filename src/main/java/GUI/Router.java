package GUI;

import SmartCity.MapAccessManager;

import org.javatuples.Pair;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.viewer.GeoPosition;

import com.graphhopper.util.PointList;

import Agents.LightManager;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class Router { // BIG REFACTOR, MOVE GETMANAGERFORLIGHTS TO MAPACCESSMANAGER AND ADD XML PARSING !!!
    public static RouteInfo generateRouteInfo(GeoPosition pointA, GeoPosition pointB) {
        Pair<List<Long>, PointList> osmWayIdsAndPointList = findRoute(pointA, pointB);
        List<OSMNode> lightsList = MapAccessManager.sendTrafficSignalOverpassQuery(osmWayIdsAndPointList.getValue0());
        List<LightManager> managers = getManagersForLights(lightsList);
        return new RouteInfo(osmWayIdsAndPointList.getValue1(), new LinkedHashSet<>(managers));
    }

    private static Pair<List<Long>, PointList> findRoute(GeoPosition pointA, GeoPosition pointB) {
        Pair<List<Long>, PointList> osmWayIdsAndPointList = SmartCity.HighwayAccessor.getOsmWayIdsAndPointList(new String[]{"config=config.properties", "datareader.file=mazowieckie-latest.osm.pbf"},
                pointA.getLatitude(), pointA.getLongitude(), pointB.getLatitude(), pointB.getLongitude());
        return osmWayIdsAndPointList;
    }
    
    private static List<LightManager> getManagersForLights(List<OSMNode> lights) {
    	List<LightManager> managers = new ArrayList<>();
    	managers.add(new LightManager()); // TODO !!!
    	return managers;
    }
}
