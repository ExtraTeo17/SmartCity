package GUI;

import com.graphhopper.util.PointList;

import Agents.LightManager;

import java.util.List;
import java.util.Set;

public class RouteInfo {
    public PointList route;
    public List<Long> lightManagers;

    public RouteInfo(PointList route, List<Long> lightManagers) {
        this.route = route;
        this.lightManagers = lightManagers;
    }
}