package OSMProxy.Elements;

import java.util.ArrayList;
import java.util.List;

import org.jxmapviewer.viewer.GeoPosition;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class OSMWay extends OSMElement {
	
	public enum LightOrientation {
		LIGHT_AT_ENTRY,
		LIGHT_AT_EXIT
	}
	
	public enum RelationOrientation {
		HOMOSEXUAL,
		HETEROSEXUAL
	}
	
	private static final String YES = "yes";
	private static final String NO = "no";

	private final List<OSMWaypoint> waypoints;
	private final List<String> childNodeIds;
	private LightOrientation lightOrientation = null;
	private RelationOrientation relationOrientation = null;
	private boolean isOneWay;
	
	public OSMWay(final String id) {
		super(id);
		waypoints = new ArrayList<>();
		childNodeIds = new ArrayList<>();
	}

	public OSMWay(Node item) {
		super(item.getAttributes().getNamedItem("id").getNodeValue());
		waypoints = new ArrayList<>();
		NodeList childNodes = item.getChildNodes();
		for (int k = 0; k < childNodes.getLength(); ++k) {
			Node el = childNodes.item(k);
			if (el.getNodeName().equals("nd")) {
				NamedNodeMap attributes = el.getAttributes();
				String nodeRef = attributes.getNamedItem("ref").getNodeValue();
				double lat = Double.parseDouble(attributes.getNamedItem("lat").getNodeValue());
				double lng = Double.parseDouble(attributes.getNamedItem("lon").getNodeValue());
				addPoint(new OSMWaypoint(nodeRef, lat, lng));
			} else if (el.getNodeName().equals("tag") &&
					el.getAttributes().getNamedItem("k").getNodeValue().equals("oneway")) {
				fillOneWay(el.getAttributes().getNamedItem("v").getNodeValue());
			}
			// consider adding other tags
		}
		childNodeIds = new ArrayList<>();
	}

	public boolean isOneWayAndLightContiguous(final long osmLightId) {
		return !(isOneWay && Long.parseLong(waypoints.get(0).getOsmNodeRef()) == osmLightId);
	}

	private void fillOneWay(final String nodeValue) {
		if (nodeValue.equals(YES))
			isOneWay = true;
		else if (nodeValue.equals(NO))
			isOneWay = false;
	}

	public void addPoint(final OSMWaypoint waypoint) {
		waypoints.add(waypoint);
	}
	
	public void addChildNodeId(final String id) {
		childNodeIds.add(id);
	}

	public final List<OSMWaypoint> getWaypoints() {
		return waypoints;
	}
	
	public final OSMWaypoint getWaypoint(int i) {
		return waypoints.get(i);
	}
	
	public final int getWaypointCount() {
		return waypoints.size();
	}
	
	public final boolean isLightOriented() {
		return lightOrientation != null;
	}
	
	public final boolean isRelationOriented() {
		return relationOrientation != null;
	}
	
	public final LightOrientation getLightOrientation() {
		return lightOrientation;
	}
	
	public final RelationOrientation getRelationOrientation() {
		return relationOrientation;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder()
				.append(super.toString())
				.append("waypoints:" + "\n");
		for (final OSMWaypoint waypoint : waypoints)
			builder.append(waypoint.getPosition());
		return builder.append("\n").toString();
	}

	public void determineLightOrientationTowardsCrossroad(final String osmLightId) {
		if (waypoints.get(0).getOsmNodeRef().equals(osmLightId))
			lightOrientation = LightOrientation.LIGHT_AT_ENTRY;
		else if (waypoints.get(waypoints.size() - 1).getOsmNodeRef().equals(osmLightId))
			lightOrientation = LightOrientation.LIGHT_AT_EXIT;
	}
	
	public String determineRelationOrientation(final String adjacentNodeRef) {
		String firstWayFirstOsmNodeRef = getWaypoint(0).getOsmNodeRef();
		String firstWayLastOsmNodeRef = getWaypoint(getWaypointCount() - 1).getOsmNodeRef();
		if (firstWayFirstOsmNodeRef.equals(adjacentNodeRef)) {
			relationOrientation = RelationOrientation.HETEROSEXUAL;
			return firstWayLastOsmNodeRef;
		} else if (firstWayLastOsmNodeRef.equals(adjacentNodeRef)) {
			relationOrientation = RelationOrientation.HOMOSEXUAL;
			return firstWayFirstOsmNodeRef;
		} else {
			try {
				throw new Exception("This orientation is not yet known :(");
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}
	
	public String determineRelationOrientation(final OSMWay nextWay) {
		String firstWayFirstOsmNodeRef = getWaypoint(0).getOsmNodeRef();
		String firstWayLastOsmNodeRef = getWaypoint(getWaypointCount() - 1).getOsmNodeRef();
		String secondWayFirstOsmNodeRef = nextWay.getWaypoint(0).getOsmNodeRef();
		String secondWayLastOsmNodeRef = nextWay.getWaypoint(nextWay.getWaypointCount() - 1).getOsmNodeRef();
		if (firstWayFirstOsmNodeRef.equals(secondWayFirstOsmNodeRef) ||
				firstWayFirstOsmNodeRef.equals(secondWayLastOsmNodeRef)) {
			relationOrientation = RelationOrientation.HOMOSEXUAL;
			return firstWayFirstOsmNodeRef;
		} else if (firstWayLastOsmNodeRef.equals(secondWayFirstOsmNodeRef) ||
				firstWayLastOsmNodeRef.equals(secondWayLastOsmNodeRef)) {
			relationOrientation = RelationOrientation.HETEROSEXUAL;
			return firstWayLastOsmNodeRef;
		} else {
			try {
				throw new Exception("This orientation is not yet known :(");
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	public GeoPosition getLightNeighborPos() {
		switch (lightOrientation) {
		case LIGHT_AT_ENTRY:
			return waypoints.get(0 + 1).getPosition();
		case LIGHT_AT_EXIT:
			return waypoints.get(waypoints.size() - 1 - 1).getPosition();
		}
		return null;
	}

	public boolean startsInCircle(int radius, double middleLat, double middleLon) {
		return getWaypoint(0).containedInCircle(radius, middleLat, middleLon);
	}
}
