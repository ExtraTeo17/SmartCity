package routing.nodes;


import routing.core.IGeoPosition;
import routing.core.Position;

public class RouteNode extends Position {
    private int internalEdgeId;

    public RouteNode(double lat, double lng) {
        super(lat, lng);
    }

    public RouteNode(double lat, double lng, int edgeId) {
        super(lat, lng);
        this.internalEdgeId = edgeId;
    }

    public RouteNode(IGeoPosition pos) {
        super(pos);
    }

    public int getInternalEdgeId() { return internalEdgeId;}

    @Override
    public String toString() {
        return "(" + getLat() + ", " + getLng() + ")\n";
    }
}
