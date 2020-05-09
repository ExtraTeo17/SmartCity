package GUI;

import SmartCity.MapAccessManager;
import SmartCity.SmartCityAgent;

import org.javatuples.Pair;
import org.jxmapviewer.viewer.GeoPosition;
import com.graphhopper.util.PointList;

import Routing.LightManagerNode;
import Routing.RouteNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class Router { // BIG REFACTOR, MOVE GETMANAGERFORLIGHTS TO MAPACCESSMANAGER AND ADD XML PARSING !!!
	
    public static List<RouteNode> generateRouteInfo(GeoPosition pointA, GeoPosition pointB) {
        Pair<List<Long>, List<RouteNode>> osmWayIdsAndPointList = findRoute(pointA, pointB);
        List<OSMNode> lightsOnRoute = MapAccessManager.sendFullTrafficSignalQuery(osmWayIdsAndPointList.getValue0());
        List<LightManagerNode> managers = getManagersForLights(lightsOnRoute);
        List<RouteNode> routeWithManagers = getRouteWithManagers(osmWayIdsAndPointList.getValue1(), managers);
        return routeWithManagers;
    }

    private static Pair<List<Long>, List<RouteNode>> findRoute(GeoPosition pointA, GeoPosition pointB) {
        Pair<List<Long>, List<RouteNode>> osmWayIdsAndPointList = SmartCity.HighwayAccessor.getOsmWayIdsAndPointList(new String[]{"config=config.properties", "datareader.file=mazowieckie-latest.osm.pbf"},
                pointA.getLatitude(), pointA.getLongitude(), pointB.getLatitude(), pointB.getLongitude());
        return osmWayIdsAndPointList;
    }
    
    private static List<LightManagerNode> getManagersForLights(List<OSMNode> lights) {
    	List<LightManagerNode> managers = new ArrayList<>();
    	Map<Long, LightManagerNode> lol = SmartCityAgent.lightIdToLightManagerNode;
    	for (OSMNode light : lights) {
    		addIfNotNull(managers, light.id);
    	}
    	return managers;
    }
    
    private static void addIfNotNull(List<LightManagerNode> managers, long lightOsmId) {
    	LightManagerNode nodeToAdd = SmartCityAgent.lightIdToLightManagerNode.get(lightOsmId);
    	if (nodeToAdd != null) {
    		managers.add(nodeToAdd);
    	}
    }
    
    private static List<RouteNode> getRouteWithManagers(List<RouteNode> route, List<LightManagerNode> managers) {
    	for (LightManagerNode node : managers) {
    		findManagerPositionOnRoute(route, node);
    	}
    	return route;
    }
    
    private static void findManagerPositionOnRoute(List<RouteNode> route, LightManagerNode manager) {
    	int minIndex1 = -1, minIndex2 = -1;
    	double minDistance1 = Double.MAX_VALUE, minDistance2 = Double.MAX_VALUE;
    	
    	for (int i = 0; i < route.size(); ++i) {
    		double distance = calculateDistance(route.get(i), manager);
    		if (distance < minDistance1) {
    			minDistance1 = distance;
    			minIndex1 = i;
    		}
    	}
    	for (int i = 0; i < route.size(); ++i) {
    		double distance = calculateDistance(route.get(i), manager);
    		if (distance < minDistance2 && minDistance2 != minDistance1) {
    			minDistance2 = distance;
    			minIndex2 = i;
    		}
    	}
    	if (minIndex1 < minIndex2) {
    		route.add(minIndex1 + 1, manager);
    	} else {
    		route.add(minIndex2 + 1, manager);
    	}
    }
    
    private static double calculateDistance(RouteNode node1, RouteNode node2) {
    	return Math.sqrt(((node2.lat - node1.lat) * (node2.lat - node1.lat))
    			+ ((node2.lon - node1.lon) * (node2.lon - node1.lon)));
    }
}
