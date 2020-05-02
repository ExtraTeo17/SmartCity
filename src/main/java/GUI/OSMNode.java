package GUI;

import org.jxmapviewer.viewer.GeoPosition;

import java.util.Map;
import java.util.Objects;

public class OSMNode {

    protected final Map<String, String> tags;
    protected long id;
    protected double lat;
    protected double lon;
    protected String version;

    public OSMNode(String id2, String latitude, String longitude, String version2, Map<String, String> tags2) {
        id = Long.parseLong(id2);
        lat = Double.parseDouble(latitude);
        lon = Double.parseDouble(longitude);
        version = version2;
        tags = tags2;
    }

    public Long getId() {
        return id;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public GeoPosition getPosition() {
        return new GeoPosition(getLat(), getLon());
    }

    @Override
    public boolean equals(Object arg) {

        OSMNode obj = (OSMNode) arg;
        return this.getId().equals(obj.getId());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.id);
        hash = 53 * hash + Objects.hashCode(this.getLat());
        hash = 53 * hash + Objects.hashCode(this.getLon());

        return hash;
    }
}