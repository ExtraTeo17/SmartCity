package GUI;

import com.graphhopper.util.PointList;

import Agents.LightManager;

import java.util.List;
import java.util.Set;

public class RouteInfo { // REFACTOR TO CONTAIN INFO ABOUT ADJACENTOSMWAYID ALONGSIDE LIGHTMANAGER AND WHEN A CROSSROAD HAS BEEN PASSED !!!
    public PointList route;
    public Set<LightManager> lightManagers;

    public RouteInfo(PointList route, Set<LightManager> lightManagers) {
        this.route = route;
        this.lightManagers = lightManagers;
    }
}