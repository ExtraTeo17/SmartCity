package SmartCity;

import java.util.ArrayList;
import java.util.List;

import org.jxmapviewer.viewer.GeoPosition;

import OSMProxy.Elements.OSMNode;
import OSMProxy.Elements.OSMWay;
import Routing.RouteNode;

public class CrossroadInfo {
	
	private static double cosineOf135degrees = -0.7071;
	
	private List<LightInfo> firstLightGroupInfo;
	private List<LightInfo> secondLightGroupInfo;

	public CrossroadInfo(OSMNode centerCrossroadNode) {
		firstLightGroupInfo = new ArrayList<>();
		secondLightGroupInfo = new ArrayList<>();
		final OSMWay firstParentWay = centerCrossroadNode.getParentWay(0);
		firstLightGroupInfo.add(new LightInfo(firstParentWay, centerCrossroadNode,
				calculateDistance(firstParentWay.getLightNeighborPos(), centerCrossroadNode.getPosition())));
		for (int i = 1; i < centerCrossroadNode.getParentWayCount(); ++i) {
			determineLightGroup(firstParentWay, centerCrossroadNode.getParentWay(i), centerCrossroadNode);
		}
	}

	private void determineLightGroup(OSMWay wayFromFirstGroup, OSMWay anotherWay, OSMNode centerNode) {
		double a = calculateDistance(wayFromFirstGroup.getLightNeighborPos(), centerNode.getPosition());
		double b = calculateDistance(anotherWay.getLightNeighborPos(), centerNode.getPosition());
		double c = calculateDistance(wayFromFirstGroup.getLightNeighborPos(), anotherWay.getLightNeighborPos());
		if (cosineFromLawOfCosinesInTriangle(a, b, c) < cosineOf135degrees)
			firstLightGroupInfo.add(new LightInfo(anotherWay, centerNode, b));
		else
			secondLightGroupInfo.add(new LightInfo(anotherWay, centerNode, b));
	}

	public List<LightInfo> getFirstLightGroupInfo() {
		return firstLightGroupInfo;
	}

	public List<LightInfo> getSecondLightGroupInfo() {
		return secondLightGroupInfo;
	}

	private final double cosineFromLawOfCosinesInTriangle(double a, double b, double c) {
		return ((a * a) + (b * b) - (c * c)) / (2 * a * b);
	}
    
    private final double calculateDistance(GeoPosition pos1, GeoPosition pos2) {
        return Math.sqrt(((pos2.getLatitude() - pos1.getLatitude()) * (pos2.getLatitude() - pos1.getLatitude()))
                + ((pos2.getLongitude() - pos1.getLongitude()) * (pos2.getLongitude() - pos1.getLongitude())));
    }
}
