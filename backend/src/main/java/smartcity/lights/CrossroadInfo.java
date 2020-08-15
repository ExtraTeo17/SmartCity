package smartcity.lights;

import osmproxy.elements.OSMNode;
import osmproxy.elements.OSMWay;

import java.util.ArrayList;
import java.util.List;

import static utilities.NumericHelper.getCosineInTriangle;
import static utilities.NumericHelper.getEuclideanDistance;

class CrossroadInfo {
    private static final double COSINE_OF_135_DEGREES = -0.7071;
    private final List<LightInfo> firstLightGroupInfo;
    private final List<LightInfo> secondLightGroupInfo;

    CrossroadInfo(OSMNode centerNode) {
        this.firstLightGroupInfo = new ArrayList<>();
        this.secondLightGroupInfo = new ArrayList<>();

        final OSMWay firstWay = centerNode.getParentWay(0);
        firstLightGroupInfo.add(new LightInfo(firstWay, centerNode,
                getEuclideanDistance(firstWay.getLightNeighborPos(), OSMNode.convertToPosition(centerNode))));
        for (int i = 1; i < centerNode.getParentWayCount(); ++i) {
            var nextParentWay = centerNode.getParentWay(i);
            var pos = OSMNode.convertToPosition(centerNode);
            double a = getEuclideanDistance(firstWay.getLightNeighborPos(), pos);
            double b = getEuclideanDistance(nextParentWay.getLightNeighborPos(), pos);
            double c = getEuclideanDistance(firstWay.getLightNeighborPos(), nextParentWay.getLightNeighborPos());
            if (getCosineInTriangle(a, b, c) < COSINE_OF_135_DEGREES) {
                firstLightGroupInfo.add(new LightInfo(nextParentWay, centerNode, b));
            }
            else {
                secondLightGroupInfo.add(new LightInfo(nextParentWay, centerNode, b));
            }
        }
    }

    List<LightInfo> getFirstLightGroupInfo() {
        return firstLightGroupInfo;
    }

    List<LightInfo> getSecondLightGroupInfo() {
        return secondLightGroupInfo;
    }


}
