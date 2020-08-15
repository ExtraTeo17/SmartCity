package osmproxy.elements;

import utilities.NumericHelper;

public class OSMLight extends OSMNode {
    private long adherentWayId;

    public OSMLight(String id, String lat, String lon) {
        super(id, lat, lon);
    }

    public final void setAdherentWayId(long value) {
        adherentWayId = value;
    }

    public final void setAdherentWayId(String value) {
        setAdherentWayId(NumericHelper.parseLong(value));
    }

    public final long getAdherentWayId() {
        return adherentWayId;
    }
}
