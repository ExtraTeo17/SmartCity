package smartcity.lights;

import osmproxy.elements.OSMNode;
import osmproxy.elements.OSMWay;

import java.util.ArrayList;
import java.util.List;

import static utilities.CalculationHelper.getCosineInTriangle;
import static utilities.CalculationHelper.getEuclideanDistance;

class CrossroadInfo {
    private static final double COSINE_OF_135_DEGREES = -0.7071;
    private final List<LightInfo> firstLightGroupInfo;
    private final List<LightInfo> secondLightGroupInfo;

    CrossroadInfo(OSMNode centerCrossroadNode) {
        firstLightGroupInfo = new ArrayList<>();
        secondLightGroupInfo = new ArrayList<>();
        final OSMWay firstParentWay = centerCrossroadNode.getParentWay(0);
        firstLightGroupInfo.add(new LightInfo(firstParentWay, centerCrossroadNode,
                getEuclideanDistance(firstParentWay.getLightNeighborPos(), centerCrossroadNode.getPosition())));
        for (int i = 1; i < centerCrossroadNode.getParentWayCount(); ++i) {
            determineLightGroup(firstParentWay, centerCrossroadNode.getParentWay(i), centerCrossroadNode);
        }
    }

    private void determineLightGroup(OSMWay wayFromFirstGroup, OSMWay anotherWay, OSMNode centerNode) {
        double a = getEuclideanDistance(wayFromFirstGroup.getLightNeighborPos(), centerNode.getPosition());
        double b = getEuclideanDistance(anotherWay.getLightNeighborPos(), centerNode.getPosition());
        double c = getEuclideanDistance(wayFromFirstGroup.getLightNeighborPos(), anotherWay.getLightNeighborPos());
        if (getCosineInTriangle(a, b, c) < COSINE_OF_135_DEGREES) {
            firstLightGroupInfo.add(new LightInfo(anotherWay, centerNode, b));
        }
        else {
            secondLightGroupInfo.add(new LightInfo(anotherWay, centerNode, b));
        }
    }

    List<LightInfo> getFirstLightGroupInfo() {
        return firstLightGroupInfo;
    }

    List<LightInfo> getSecondLightGroupInfo() {
        return secondLightGroupInfo;
    }


}
