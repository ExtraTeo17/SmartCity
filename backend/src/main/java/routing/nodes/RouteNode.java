package routing.nodes;


import routing.core.IGeoPosition;
import routing.core.Position;

public class RouteNode extends Position {
    private int internalEdgeId;
    private final boolean virtual;

    public RouteNode(double lat, double lng) {
        super(lat, lng);
        virtual = true;
    }

    public RouteNode(double lat, double lng, int edgeId) {
        super(lat, lng);
        this.internalEdgeId = edgeId;
        virtual = true;
    }

    public RouteNode(IGeoPosition pos) {
        super(pos);
        virtual = true;
    }


    public RouteNode(double lat, double lng, boolean virtual) {
        super(lat, lng);
        this.virtual = virtual;
    }

    public void setInternalEdgeId(int edgeId) { this.internalEdgeId = edgeId; }

    public int getInternalEdgeId() { return internalEdgeId;}

    public boolean isVirtual() {
        return virtual;
    }

    @Override
    public String toString() {
        return "(" + getLat() + ", " + getLng() + ")\n";
    }

    public String getDebugString(boolean isStationNode) {
        return "[" +
                (isStationNode ? "SN" : "RN") +
                ": " +
                "(" + getLat() + ", " + getLng() + ")" +
                "]";
    }
}
