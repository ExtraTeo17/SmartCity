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
    
    
    public void setInternalEdgeId(int edgeId) { this.internalEdgeId = edgeId; }

    public int getInternalEdgeId() { return internalEdgeId;}

    @Override
    public String toString() {
        return "(" + getLat() + ", " + getLng() + ")\n";
    }

    public final String getDebugString(boolean isLightManager) {
        final StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(isLightManager ? "LMN" : "RN");
        builder.append(": ");
        builder.append("(" + getLat() + ", " + getLng() + ")");
        builder.append("]");
        return builder.toString();
    }
}
