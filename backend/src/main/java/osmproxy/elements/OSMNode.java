package osmproxy.elements;

import org.jxmapviewer.viewer.GeoPosition;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

public class OSMNode extends OSMElement {

    protected final List<OSMWay> parentWays;
    protected double lat;
    protected double lon;

    public OSMNode(final String id, final String lat, final String lon) {
        super(id);
        fillLatLon(lat, lon);
        parentWays = new ArrayList<>();
    }

    public OSMNode(final OSMNode node) {
        super(node.getId());
        fillLatLon(node.lat, node.lon);
        parentWays = new ArrayList<>();
    }

    public OSMNode(final NamedNodeMap attributes) {
        super(attributes.getNamedItem("id").getNodeValue());
        fillLatLon(attributes.getNamedItem("lat"), attributes.getNamedItem("lon"));
        parentWays = new ArrayList<>();
    }

    private void fillLatLon(final Node latItem, final Node lonItem) {
        fillLatLon(latItem.getNodeValue(), lonItem.getNodeValue());
    }

    private void fillLatLon(final String lat, final String lon) {
        fillLatLon(Double.parseDouble(lat), Double.parseDouble(lon));
    }

    private void fillLatLon(final double lat, final double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public final double getLat() {
        return lat;
    }

    public final double getLon() {
        return lon;
    }

    public final void addParentWay(final OSMWay osmWay) {
        parentWays.add(osmWay);
    }

    public final int getParentWayCount() {
        return parentWays.size();
    }

    public OSMWay getParentWay(int i) {
        return parentWays.get(i);
    }

    public final boolean isTypeA() {
        return getParentWayCount() > 1;
    }

    public final GeoPosition getPosition() {
        return new GeoPosition(getLat(), getLon());
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(super.toString())
                .append("parentWays:")
                .append("\n");
        for (final OSMWay way : parentWays) {
            builder.append(way.toString());
        }
        return builder.toString();
    }

    public final void addChildNodeIdForParentWay(int parentWayIndex, String id) {
        parentWays.get(parentWayIndex).addChildNodeId(id);
    }

    public final boolean determineParentOrientationsTowardsCrossroad() {
        for (final OSMWay way : parentWays) {
            way.determineLightOrientationTowardsCrossroad(Long.toString(id));
            if (!way.isLightOriented()) {
                return false;
            }
        }
        return true;
    }
}
