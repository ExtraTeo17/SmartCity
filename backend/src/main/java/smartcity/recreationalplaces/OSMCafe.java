package smartcity.recreationalplaces;

import osmproxy.elements.OSMNode;

import java.io.Serializable;

public class OSMCafe extends OSMNode implements Serializable {
    public OSMCafe(String id, String lat, String lon) {
        super(id, lat, lon);
    }
    public OSMCafe(long id, double lat, double lon) {
        super(id, lat, lon);
    }
}
