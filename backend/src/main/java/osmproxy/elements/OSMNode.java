package osmproxy.elements;

import org.jxmapviewer.viewer.GeoPosition;
import org.w3c.dom.NamedNodeMap;
import utilities.NumericHelper;

import java.util.ArrayList;
import java.util.List;

public class OSMNode extends OSMElement {
    protected final List<OSMWay> parentWays;
    protected double lat;
    protected double lon;

    OSMNode(long id, long lat, long lon) {
        super(id);
        this.lat = lat;
        this.lon = lon;
        this.parentWays = new ArrayList<>();
    }

    @SuppressWarnings("FeatureEnvy")
    public OSMNode(final String id, final String lat, final String lon) {
        this(NumericHelper.parseLong(id),
                NumericHelper.parseLong(lat),
                NumericHelper.parseLong(lon));
    }

    public OSMNode(final NamedNodeMap attributes) {
        this(attributes.getNamedItem("id").getNodeValue(),
                attributes.getNamedItem("lat").getNodeValue(),
                attributes.getNamedItem("lon").getNodeValue());
    }

    public final double getLatitude() {
        return lat;
    }

    public final double getLongitude() {
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

    public static GeoPosition convertToPosition(OSMNode node) {
        return new GeoPosition(node.lat, node.lon);
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
