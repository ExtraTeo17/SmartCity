package GUI;

import org.jxmapviewer.viewer.GeoPosition;

import java.util.Map;
import java.util.Objects;

public class OSMNode {

    protected Map<String, String> tags = null;
    protected long id;
    protected double lat;
    protected double lon;
    protected String version;

    public OSMNode(String id, String latitude, String longitude, String version, Map<String, String> tags) {
    	fillIdLatLon(id, latitude, longitude);
        this.version = version;
        this.tags = tags;
    }

    public OSMNode(String id, String latitude, String longitude) {
    	fillIdLatLon(id, latitude, longitude);
    }
    
    public OSMNode(OSMNode node) {
		id = node.id;
		lat = node.lat;
		lon = node.lon;
	}

	private void fillIdLatLon(String id, String latitude, String longitude) {
        this.id = Long.parseLong(id);
        lat = Double.parseDouble(latitude);
        lon = Double.parseDouble(longitude);
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