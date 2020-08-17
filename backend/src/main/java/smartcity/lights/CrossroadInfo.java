package smartcity.lights;

import com.google.common.collect.Iterables;
import osmproxy.elements.OSMNode;
import osmproxy.elements.OSMWay;

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
        firstLightGroupInfo.add(new LightInfo(firstWay, centerNode));
        for (OSMWay parentWay : Iterables.skip(centerNode, 1)) {
            var nextWayLightNeighborPos = parentWay.getLightNeighborPos();
            double cosineCenterNodeNextWay = firstWayNeighborPos.cosineAngle(centerNode, nextWayLightNeighborPos);
            if (cosineCenterNodeNextWay < COSINE_OF_135_DEGREES) {
                firstLightGroupInfo.add(new LightInfo(parentWay, centerNode));
            }
            else {
                secondLightGroupInfo.add(new LightInfo(parentWay, centerNode));
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