package osmproxy.elements;

public class OSMLight extends OSMNode {
    private long adherentWayId;

    public OSMLight(String id, String lat, String lon) {
        super(id, lat, lon);
    }

    private void setAdherentWayId(long value) {
        adherentWayId = value;
    }

    public final void setAdherentWayId(String value) {
        setAdherentWayId(Long.parseLong(value));
    }

    public final long getAdherentWayId() {
        return adherentWayId;
    }
}
