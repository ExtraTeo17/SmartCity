package smartcity.lights.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import osmproxy.elements.OSMNode;
import osmproxy.elements.OSMWay;

import java.util.ArrayList;
import java.util.List;

class CrossroadInfo {
    private static final Logger logger = LoggerFactory.getLogger(CrossroadInfo.class);
    private static final double COSINE_OF_135_DEGREES = -0.7071;
    private final List<LightInfo> firstLightGroupInfo;
    private final List<LightInfo> secondLightGroupInfo;

    CrossroadInfo(OSMNode centerNode) {
        this.firstLightGroupInfo = new ArrayList<>();
        this.secondLightGroupInfo = new ArrayList<>();

        var waysIter = centerNode.getParentWaysIterator();
        if (!waysIter.hasNext()) {
            logger.warn("Empty node: " + centerNode.getId());
            return;
        }

        OSMWay firstWay = waysIter.next();
        var firstWayNeighborPos = firstWay.getLightNeighborPos();
        firstLightGroupInfo.add(new LightInfo(firstWay, centerNode));
        for (OSMWay parentWay = waysIter.next(); waysIter.hasNext(); parentWay = waysIter.next()) {
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
