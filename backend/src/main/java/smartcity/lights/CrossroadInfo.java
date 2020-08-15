package smartcity.lights;

import com.google.common.collect.Iterables;
import osmproxy.elements.OSMNode;
import osmproxy.elements.OSMWay;
import utilities.NumericHelper;

import java.util.ArrayList;
import java.util.List;

class CrossroadInfo {
    private static final double COSINE_OF_135_DEGREES = -0.7071;
    private final List<LightInfo> firstLightGroupInfo;
    private final List<LightInfo> secondLightGroupInfo;

    CrossroadInfo(OSMNode centerNode) {
        this.firstLightGroupInfo = new ArrayList<>();
        this.secondLightGroupInfo = new ArrayList<>();

        OSMWay firstWay = centerNode.iterator().next();
        var firstWayNeighborPos = firstWay.getLightNeighborPos();
        var firstWayDistance = firstWayNeighborPos.distance(centerNode);
        firstLightGroupInfo.add(new LightInfo(firstWay, centerNode, firstWayDistance));
        for (OSMWay parentWay : Iterables.skip(centerNode, 1)) {
            var nextWayLightNeighborPos = parentWay.getLightNeighborPos();
            double b = nextWayLightNeighborPos.distance(centerNode);
            double c = firstWayNeighborPos.distance(nextWayLightNeighborPos);
            if (NumericHelper.getCosineInTriangle(firstWayDistance, b, c) < COSINE_OF_135_DEGREES) {
                firstLightGroupInfo.add(new LightInfo(parentWay, centerNode, b));
            }
            else {
                secondLightGroupInfo.add(new LightInfo(parentWay, centerNode, b));
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
