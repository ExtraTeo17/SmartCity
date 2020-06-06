package GUI;

import SmartCity.MapAccessManager;
import SmartCity.OSMWay;
import SmartCity.SmartCityAgent;

import org.javatuples.Pair;
import org.jxmapviewer.viewer.GeoPosition;
import com.graphhopper.util.PointList;

import Routing.LightManagerNode;
import Routing.RouteNode;
import Routing.StationNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public final class Router { // BIG REFACTOR, MOVE GETMANAGERFORLIGHTS TO MAPACCESSMANAGER AND ADD XML PARSING !!!

    public static List<RouteNode> generateRouteInfo(GeoPosition pointA, GeoPosition pointB) {
        Pair<List<Long>, List<RouteNode>> osmWayIdsAndPointList = findRoute(pointA, pointB);
        List<OSMLight> lightsOnRoute = MapAccessManager.sendFullTrafficSignalQuery(osmWayIdsAndPointList.getValue0());
        List<RouteNode> managers = getManagersForLights(lightsOnRoute, osmWayIdsAndPointList.getValue1());
        List<RouteNode> routeWithManagers = getRouteWithAdditionalNodes(osmWayIdsAndPointList.getValue1(), managers);
        return routeWithManagers;
    }

    private static Pair<List<Long>, List<RouteNode>> findRoute(GeoPosition pointA, GeoPosition pointB) {
        Pair<List<Long>, List<RouteNode>> osmWayIdsAndPointList = SmartCity.HighwayAccessor.getOsmWayIdsAndPointList(new String[]{"config=config.properties", "datareader.file=mazowieckie-latest.osm.pbf"},
                pointA.getLatitude(), pointA.getLongitude(), pointB.getLatitude(), pointB.getLongitude());
        return osmWayIdsAndPointList;
    }

    private static List<RouteNode> getManagersForLights(List<OSMLight> lights, List<RouteNode> route) {
        List<RouteNode> managers = new ArrayList<>();
        for (OSMLight light : lights) {
            addLightManagerNodeToManagersListIfItsNotNullAfterGettingItFromSmartCityAgentById(managers, light, route);
        }
        return managers;
    }

    private static void addLightManagerNodeToManagersListIfItsNotNullAfterGettingItFromSmartCityAgentById(List<RouteNode> managers, OSMLight light, List<RouteNode> route) {
    	Pair<Long, Long> osmWayIdOsmLightId = Pair.with(light.adherentOsmWayId, light.id);
    	RouteNode nodeToAdd = SmartCityAgent.wayIdLightIdToLightManagerNode.get(osmWayIdOsmLightId);
    	/*Long lightManagerIdToAdd = SmartCityAgent.lightIdToLightManagerId.get(light.getId());
    	if (lightManagerIdToAdd == null)
    		return;*/
    	//RouteNode nodeToAdd = new LightManagerNode(light.lat, light.lon, light.adherentOsmWayId, lightManagerIdToAdd);
        if (nodeToAdd != null && !lastManagerIdEqualTo(managers, nodeToAdd)/* && routeContainsOsmWayId(nodeToAdd.getOsmWayId(), route)*/)
            managers.add(nodeToAdd);
    }

    private static boolean lastManagerIdEqualTo(List<RouteNode> managers, RouteNode nodeToAdd) {
		if (managers.size() == 0)
			return false;
		LightManagerNode lastNodeOnList = (LightManagerNode)managers.get(managers.size() - 1);
		return lastNodeOnList.getLightManagerId() == ((LightManagerNode)nodeToAdd).getLightManagerId();
	}

	private static boolean routeContainsOsmWayId(long osmWayId, List<RouteNode> route) {
		for (RouteNode node : route) {
			if (node.getOsmWayId() == osmWayId)
				return true;
		}
		return false;
	}

	private static List<RouteNode> getRouteWithAdditionalNodes(List<RouteNode> route, List<RouteNode> more_nodes) {
        for (RouteNode node : more_nodes) {
            findPositionOfElementOnRoute(route, node);
        }
        return route;
    }

    private static void findPositionOfElementOnRoute(List<RouteNode> route, RouteNode manager) {
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

    public static List<RouteNode> uniformRoute(List<RouteNode> route) {
        List<RouteNode> newRoute = new ArrayList<>();

        for (int i = 0; i < route.size() - 1; i++) {
            RouteNode A = route.get(i);
            RouteNode B = route.get(i + 1);
            double x = B.getLongitude() - A.getLongitude();
            double y = B.getLatitude() - A.getLatitude();

            double xInMeters = x * 111111;
            double yInMeters = y * 111111;

            double distance = Math.sqrt(xInMeters * xInMeters + yInMeters * yInMeters);

            double dx = x / distance;
            double dy = y / distance;

            newRoute.add(A);

            double lon = A.getLongitude();
            double lat = A.getLatitude();

            for (int p = 1; p < distance; p++) {
                lon = lon + dx;
                lat = lat + dy;
                RouteNode node = new RouteNode(lat, lon, A.getOsmWayId());
                newRoute.add(node);
            }
        }

        newRoute.add(route.get(route.size() - 1));

        return newRoute;
    }

    private static double calculateDistance(RouteNode node1, RouteNode node2) {
        return Math.sqrt(((node2.getLatitude() - node1.getLatitude()) * (node2.getLatitude() - node1.getLatitude()))
                + ((node2.getLongitude() - node1.getLongitude()) * (node2.getLongitude() - node1.getLongitude())));
    }
    
    
    public static List<RouteNode> generateRouteInfoForBuses(List<OSMWay> router, List<Long> osmStationIds) {
    	   Pair<List<Long>, List<RouteNode>> osmWayIdsAndPointList = findBusRoute(router);
    	 // REPAIR BUSES AFTER 01.06.2020 CHANGES
           List<OSMLight> lightsOnRoute = MapAccessManager.sendFullTrafficSignalQuery(osmWayIdsAndPointList.getValue0());
           List<RouteNode> managers = getManagersForLights(lightsOnRoute, osmWayIdsAndPointList.getValue1());
           List<RouteNode> stationNodes = getAgentStationsForRoute(getOSMNodesForStations(osmStationIds), osmWayIdsAndPointList.getValue1());
           managers.addAll(stationNodes);
           List<RouteNode> routeWithManagers = getRouteWithAdditionalNodes(osmWayIdsAndPointList.getValue1(), managers);
           return routeWithManagers;
    }

    
    
    private static List<OSMNode> getOSMNodesForStations(List<Long> stationsIDs) {
    	List<OSMNode> listOsmNodes = new ArrayList<>();
		for (long station : stationsIDs)
		{
			listOsmNodes.add(SmartCityAgent.osmIdToStationOSMNode.get(station));
		}
		return listOsmNodes;
	}

	private static Pair<List<Long>, List<RouteNode>> findBusRoute(List<OSMWay> router) {
    	
    	List<Long> osmWayIds_list = new ArrayList<>();
    	List<RouteNode> RouteNodes_list = new ArrayList<>();
		for(OSMWay el : router)
		{
			osmWayIds_list.add(el.getOsmWayId());
			for(GeoPosition point : el.getCoordinates()) {
				RouteNodes_list.add(new RouteNode(point.getLatitude(),point.getLongitude(),el.getOsmWayId()));
			}

		}
		   
		return  new Pair<List<Long>, List<RouteNode>>(osmWayIds_list,RouteNodes_list);
	}


    private static List<RouteNode> getAgentStationsForRoute(List<OSMNode> stations, List<RouteNode> route) {
        List<RouteNode> managers = new ArrayList<>();
        /*for (OSMNode station : stations) {
            addLightManagerNodeToManagersListIfItsNotNullAfterGettingItFromSmartCityAgentById(managers, station, route);
        }*/
        // UNCOMMENT AFTER FIXING !!!
        return managers;
    }

	public static List<RouteNode> generateRouteInfoForPedestrians(GeoPosition pedestrianStartPoint,
			GeoPosition pedestrianGetOnStation) {
		try {
			throw new Exception("Not implemented!");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
