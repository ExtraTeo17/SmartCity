package GUI;

import com.graphhopper.util.PointList;

import java.util.List;
import java.util.Set;

public class RouteInfo {
    public PointList Route;
    public Set<OSMNode> Lights;

    public RouteInfo(PointList route, Set<OSMNode> lights) {
        Route = route;
        Lights = lights;
    }
}