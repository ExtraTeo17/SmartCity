package Routing;

import SmartCity.SmartCityAgent;

import org.javatuples.Pair;
import org.jxmapviewer.viewer.GeoPosition;

import OSMProxy.MapAccessManager;
import OSMProxy.Elements.OSMLight;
import OSMProxy.Elements.OSMNode;
import OSMProxy.Elements.OSMWay;
import OSMProxy.Elements.OSMWay.RelationOrientation;
import OSMProxy.Elements.OSMWay.RouteOrientation;
import OSMProxy.Elements.OSMWaypoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class Router {

    public static List<RouteNode> generateRouteInfo(GeoPosition pointA, GeoPosition pointB) {
        Pair<List<Long>, List<RouteNode>> osmWayIdsAndPointList = findRoute(pointA, pointB, false);
        final List<OSMLight> lightInfo = MapAccessManager.sendFullTrafficSignalQuery(osmWayIdsAndPointList.getValue0());
        List<RouteNode> managers = getManagersForLights(lightInfo, osmWayIdsAndPointList.getValue1());
        List<RouteNode> routeWithManagers = getRouteWithAdditionalNodes(osmWayIdsAndPointList.getValue1(), managers);
        return routeWithManagers;
    }

    @Deprecated
	public static List<RouteNode> generateRouteInfoForPedestrians(GeoPosition pointA, GeoPosition pointB) { // TODO: Merge with function for cars if testing proves they are identical
		Pair<List<Long>, List<RouteNode>> osmWayIdsAndPointList = findRoute(pointA, pointB, true);
		final List<OSMLight> lightInfo = MapAccessManager.sendFullTrafficSignalQuery(osmWayIdsAndPointList.getValue0());
		List<RouteNode> managers = getManagersForLights(lightInfo, osmWayIdsAndPointList.getValue1());
		List<RouteNode> routeWithMgrs = getRouteWithAdditionalNodes(osmWayIdsAndPointList.getValue1(), managers);
		return routeWithMgrs;
	}
    
    public static List<RouteNode> generateRouteInfoForPedestriansBeta(GeoPosition pointA, GeoPosition pointB, // TODO: Improve routing to consider random OSM nodes as start/end points instead of random lat/lons
    		String startingOsmNodeRef, String finishingOsmNodeRef) {
    	Pair<List<Long>, List<RouteNode>> osmWayIdsAndPointList = findRoute(pointA, pointB, true);
    	final RouteInfo routeInfo = MapAccessManager.sendMultipleWayAndItsNodesQuery(osmWayIdsAndPointList.getValue0());
    	return createRouteNodeList(routeInfo, startingOsmNodeRef, finishingOsmNodeRef);
    }

	public static List<RouteNode> generateRouteInfoForBuses(List<OSMWay> router, List<Long> osmStationIds) {
	    Pair<List<Long>, List<RouteNode>> osmWayIdsAndPointList = findBusRoute(router);
	    List<OSMLight> lightsOnRoute = MapAccessManager.sendFullTrafficSignalQuery(osmWayIdsAndPointList.getValue0());
	    List<RouteNode> managers = getManagersForLights(lightsOnRoute, osmWayIdsAndPointList.getValue1());
	    List<RouteNode> stationNodes = getAgentStationsForRoute(getOSMNodesForStations(osmStationIds), osmWayIdsAndPointList.getValue1());
	    managers.addAll(stationNodes);
	    List<RouteNode> routeWithManagers = getRouteWithAdditionalNodes(osmWayIdsAndPointList.getValue1(), managers);
	    return routeWithManagers;
    }

	private static List<RouteNode> createRouteNodeList(RouteInfo routeInfo, String startingOsmNodeRef, String finishingOsmNodeRef) {
		routeInfo.determineRouteOrientationsAndFilterRelevantNodes(startingOsmNodeRef, finishingOsmNodeRef);
		List<RouteNode> routeNodes = new ArrayList<>();
		List<Long> crossingOsmIdsToTransform = new ArrayList<>();
		for (int i = 0; i < routeInfo.getWayCount(); ++i) {
			if (routeInfo.getWay(i).getRouteOrientation() == RouteOrientation.HETEROSEXUAL) {
				for (int j = 0; j < routeInfo.getWay(i).getWaypointCount(); ++j) {
					addRouteNode(routeNodes, routeInfo.getWay(i).getWaypoint(j), routeInfo, crossingOsmIdsToTransform);
				}
			} else {
				for (int j = routeInfo.getWay(i).getWaypointCount() - 1; j >= 0; --j) {
					addRouteNode(routeNodes, routeInfo.getWay(i).getWaypoint(j), routeInfo, crossingOsmIdsToTransform);
				}
			}
		}
		return routeNodes;
	}

	private static void addRouteNode(List<RouteNode> routeNodes, OSMWaypoint waypoint, RouteInfo routeInfo,
			List<Long> crossingOsmIdsToTransform) {
		if (routeInfo.removeIfContains(waypoint.getOsmNodeRef())) {
			routeNodes.add(SmartCityAgent.crossingOsmIdToLightManagerNode.get(Long.parseLong(waypoint.getOsmNodeRef())));
		} else {
			routeNodes.add(new RouteNode(waypoint.getPosition()));
		}
	}
    
    /////////////////////////////////////////////////////////////
    //  HELPERS
    /////////////////////////////////////////////////////////////

    private static Pair<List<Long>, List<RouteNode>> findRoute(GeoPosition pointA, GeoPosition pointB, boolean onFoot) {
        Pair<List<Long>, List<RouteNode>> osmWayIdsAndPointList = OSMProxy.HighwayAccessor.getOsmWayIdsAndPointList(new String[]{"config=config.properties", "datareader.file=mazowieckie-latest.osm.pbf"},
                pointA.getLatitude(), pointA.getLongitude(), pointB.getLatitude(), pointB.getLongitude(), onFoot);
        return osmWayIdsAndPointList;
    }

    private static List<RouteNode> getManagersForLights(List<OSMLight> lights, List<RouteNode> route) {
        List<RouteNode> managers = new ArrayList<>();
        for (OSMLight light : lights) {
            addLightManagerNodeToManagersList(managers, light, route);
        }
        return managers;
    }

    private static void addLightManagerNodeToManagersList(List<RouteNode> managers, OSMLight light, List<RouteNode> route) {
    	Pair<Long, Long> osmWayIdOsmLightId = Pair.with(light.getAdherentOsmWayId(), light.getId());
    	RouteNode nodeToAdd = SmartCityAgent.wayIdLightIdToLightManagerNode.get(osmWayIdOsmLightId);
    	
        if (nodeToAdd != null && !lastManagerIdEqualTo(managers, nodeToAdd))
            managers.add(nodeToAdd);
    }

    private static boolean lastManagerIdEqualTo(List<RouteNode> managers, RouteNode nodeToAdd) {
		if (managers.size() == 0)
			return false;
		LightManagerNode lastNodeOnList = (LightManagerNode)managers.get(managers.size() - 1);
		return lastNodeOnList.getLightManagerId() == ((LightManagerNode)nodeToAdd).getLightManagerId();
	}


	private static List<RouteNode> getRouteWithAdditionalNodes(List<RouteNode> route, List<RouteNode> more_nodes) {
        for (RouteNode node : more_nodes) {
            findPositionOfElementOnRoute(route, node);
        }
        return route;
    }

 
	private static void findPositionOfElementOnRoute(List<RouteNode> route, RouteNode manager) {
        int minIndex = -1;
        double minDistance = Double.MAX_VALUE;
        for (int i = 0; i < route.size(); ++i) {
            double distance = calculateDistance(route.get(i), manager);
            if (distance < minDistance) {
                minDistance = distance;
                minIndex = i;
            }
        }
        if (minIndex <= 0) {
        	route.add(minIndex + 1, manager);
        	return;
        }
        double distMgrToMinPrev = calculateDistance(route.get(minIndex - 1), manager);
        double distMinToMinPrev = calculateDistance(route.get(minIndex - 1), route.get(minIndex));
        if (distMgrToMinPrev < distMinToMinPrev)
        	route.add(minIndex, manager);
        else
        	route.add(minIndex + 1, manager);
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
                RouteNode node = new RouteNode(lat, lon/*, A.getOsmWayId()*/);
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
    
    private static List<OSMNode> getOSMNodesForStations(List<Long> stationsIDs) {
    	List<OSMNode> listOsmNodes = new ArrayList<>();
		for (long station : stationsIDs) {
			listOsmNodes.add(SmartCityAgent.osmIdToStationOSMNode.get(station));
		}
		return listOsmNodes;
	}

	private static Pair<List<Long>, List<RouteNode>> findBusRoute(List<OSMWay> router) {
    	List<Long> osmWayIds_list = new ArrayList<>();
    	List<RouteNode> RouteNodes_list = new ArrayList<>();
		for (OSMWay el : router) {
			osmWayIds_list.add(el.getId());
			addRouteNodesBasedOnOrientation(el, RouteNodes_list);
		}
		return new Pair<List<Long>, List<RouteNode>>(osmWayIds_list,RouteNodes_list);
	}
	
	private static void addRouteNodesBasedOnOrientation(OSMWay el, List<RouteNode> routeNodes_list) {
		if (el.getRelationOrientation() == RelationOrientation.HETEROSEXUAL) {
			addRouteNodesToList(el.getWaypoints(), routeNodes_list);
		} else if (el.getRelationOrientation() == RelationOrientation.HOMOSEXUAL) {
			addRouteNodesToList(reverse(el.getWaypoints()), routeNodes_list);
		} else {
			try {
				throw new Exception("Orientation was not known :(");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

    private static List<OSMWaypoint> reverse(List<OSMWaypoint> waypoints) {
		List<OSMWaypoint> waypointsCopy = new ArrayList<>(waypoints);
		Collections.reverse(waypointsCopy);
		return waypointsCopy;
	}

	private static void addRouteNodesToList(List<OSMWaypoint> waypoints, List<RouteNode> routeNodes_list) {
		for (OSMWaypoint point : waypoints) {
			routeNodes_list.add(new RouteNode(point.getLat(),point.getLon()));
		}
	}

	private static List<RouteNode> getAgentStationsForRoute(List<OSMNode> stations, List<RouteNode> route) {
        List<RouteNode> managers = new ArrayList<>();
        for (OSMNode station : stations) {
          addStationNodeToList(managers, station, route);
       }
       return managers;
    }

	private static void addStationNodeToList(List<RouteNode> stations, OSMNode station, List<RouteNode> route) {
	
    	RouteNode nodeToAdd = SmartCityAgent.osmStationIdToStationNode.get(station.getId());
    	stations.add(nodeToAdd);
	}
}
