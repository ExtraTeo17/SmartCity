package smartcity.lights;

import com.google.common.collect.Iterables;
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

        var waysIter = centerNode.iterator();
        if (!waysIter.hasNext()) {
            logger.warn("Empty node: " + centerNode.getId());
            return;
        }

        OSMWay firstWay = waysIter.next();
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
