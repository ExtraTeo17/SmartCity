package GUI;

import org.jxmapviewer.viewer.GeoPosition;

import java.util.Map;
import java.util.Objects;

public class OSMNode {

    private final Map<String, String> tags;
    private String id;

    private String lat;

    private String lon;
    private String version;

    public OSMNode(String id2, String latitude, String longitude, String version2, Map<String, String> tags2) {
        id = id2;
        lat = latitude;
        lon = longitude;
        version = version2;
        tags = tags2;
    }

    public Long getId() {
        return Long.parseLong(id);
    }

    public double getLat() {
        return Double.parseDouble(lat);
    }

    public double getLon() {
        return Double.parseDouble(lon);
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