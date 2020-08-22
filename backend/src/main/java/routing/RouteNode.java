package routing;


import routing.core.IGeoPosition;
import routing.core.Position;

public class RouteNode extends Position {
    public RouteNode(double lat, double lng) {
        super(lat, lng);
    }

    public RouteNode(IGeoPosition pos) {
        super(pos);
    }

    @Override
    public String toString() {
        return "(" + getLat() + ", " + getLng() + ")\n";
    }
}
