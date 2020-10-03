package osmproxy.elements;


import org.w3c.dom.NamedNodeMap;
import routing.core.IGeoPosition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OSMNode extends OSMElement
        implements IGeoPosition {

    protected final List<OSMWay> parentWays;
    protected final double lat;
    protected final double lon;

    OSMNode(long id, double lat, double lon) {
        super(id);
        this.lat = lat;
        this.lon = lon;
        this.parentWays = new ArrayList<>();
    }

    public OSMNode(final String id, final String lat, final String lon) {
        this(Long.parseLong(id),
                Double.parseDouble(lat),
                Double.parseDouble(lon));
    }

    public OSMNode(final NamedNodeMap attributes) {
        this(attributes.getNamedItem("id").getNodeValue(),
                attributes.getNamedItem("lat").getNodeValue(),
                attributes.getNamedItem("lon").getNodeValue());
    }

    @Override
    public final double getLat() {
        return lat;
    }

    @Override
    public final double getLng() {
        return lon;
    }

    public void addParentWay(final OSMWay osmWay) {
        parentWays.add(osmWay);
    }

    public Iterator<OSMWay> getParentWaysIterator() {return parentWays.iterator();}

    public boolean isTypeA() {
        return parentWays.size() > 1;
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

    // TODO: Change name to define the purpose, not the implementation - isLightOriented?
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
