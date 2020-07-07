package Deprecated;

import OSMProxy.Elements.OSMNode;
import OSMProxy.MapAccessManager;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class HighwayAccessor {

    public Set<Long> getHighways(PointList route) {
        PointList doublePointedRoute = doubleAllPoints(route);
        long time1 = System.nanoTime();
        List<OSMNode> notUniqueAndUnwantedHighways = getAllHighwaysFromRoute(doublePointedRoute);
        long time2 = System.nanoTime();
        Set<Long> finalHighways = filterUnwantedHighways(notUniqueAndUnwantedHighways);
        long time3 = System.nanoTime();
        System.out.println("Funkcja overpassowa zajela: " + ((time2 - time1) / 1000000000));
        System.out.println("Nasza <333 funkcja zajela: " + ((time3 - time2) / 1000000000));
        return finalHighways;
    }

    private PointList doubleAllPoints(PointList route) {
        PointList doubledRoute = new PointList();
        for (int i = 0; i < route.size() - 1; ++i) {
            addPair(route, doubledRoute, i);
        }
        //doubledRoute.add(route.toGHPoint(route.size() - 1));
        return doubledRoute;
    }

    private void addPair(PointList route, PointList doubledRoute, int i) {
        //GHPoint firstPoint = route.toGHPoint(i);
        //GHPoint secondPoint = route.toGHPoint(i + 1);
        //doubledRoute.add(firstPoint);
        //if (getDistBetween(firstPoint, secondPoint) > 2) {
        //doubledRoute.add(getMiddlePoint(firstPoint, secondPoint));
        //}
    }

    private double getDistBetween(GHPoint point1, GHPoint point2) {
        return Math.sqrt(((point1.lat - point2.lat) * (point1.lat - point2.lat)) -
                ((point1.lon - point2.lon) * (point1.lon - point2.lon)));
    }

    private GHPoint getMiddlePoint(GHPoint point1, GHPoint point2) {
        return new GHPoint((point1.lat + point2.lat) / 2, (point1.lon + point2.lon) / 2);
    }

    private List<OSMNode> getAllHighwaysFromRoute(PointList doublePointedRoute) {
        List<OSMNode> highways = MapAccessManager.sendHighwayOverpassQuery(doublePointedRoute);
        return highways;
    }

    private Set<Long> filterUnwantedHighways(List<OSMNode> notUniqueHighways) {
        List<Long> highwayOsmIds = highwayListToOsmIdList(notUniqueHighways);
        Set<Long> highways = findDuplicates(highwayOsmIds);
        return highways;
    }

    private List<Long> highwayListToOsmIdList(List<OSMNode> highways) {
        List<Long> osmIdList = new ArrayList<>();
        for (OSMNode node : highways) {
            osmIdList.add(node.getId());
        }
        return osmIdList;
    }

    private Set<Long> findDuplicates(List<Long> listContainingDuplicates) {
        final Set<Long> setToReturn = new LinkedHashSet<>();
        final Set<Long> set1 = new LinkedHashSet<>();
        for (Long yourLong : listContainingDuplicates) {
            if (!set1.add(yourLong)) {
                setToReturn.add(yourLong);
            }
        }
        return setToReturn;
    }
}
