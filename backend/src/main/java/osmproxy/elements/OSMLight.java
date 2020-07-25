package osmproxy.elements;

public class OSMLight extends OSMNode {

    protected final long adherentOsmWayId;

    public OSMLight(OSMNode node, String adherentOsmWayId) {
        super(node);
        this.adherentOsmWayId = Long.parseLong(adherentOsmWayId);
    }

    public final long getAdherentOsmWayId() {
        return adherentOsmWayId;
    }
}
