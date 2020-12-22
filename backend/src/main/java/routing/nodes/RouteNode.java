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

    public final boolean isVirtual() {
        return virtual;
    }

    @Override
    public String toString() {
        return "(" + getLat() + ", " + getLng() + ")\n";
    }

    public final String getDebugString(boolean isStationNode) {
        final StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(isStationNode ? "SN" : "RN");
        builder.append(": ");
        builder.append("(" + getLat() + ", " + getLng() + ")");
        builder.append("]");
        return builder.toString();
    }
}
