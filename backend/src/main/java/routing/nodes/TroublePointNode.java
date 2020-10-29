package routing.nodes;

import routing.core.IGeoPosition;

public class TroublePointNode extends RouteNode {
    private final int id;

    public TroublePointNode(int id, double lat, double lng) {
        super(lat, lng);
        this.id = id;
    }

    public TroublePointNode(int id, double lat, double lng, int edgeId) {
        super(lat, lng, edgeId);
        this.id = id;
    }

    public TroublePointNode(int id, IGeoPosition pos) {
        super(pos);
        this.id = id;
    }
}
