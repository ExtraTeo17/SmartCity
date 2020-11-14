package smartcity.lights.core;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import org.w3c.dom.Node;
import osmproxy.elements.OSMNode;
import smartcity.config.abstractions.ITroublePointsConfigContainer;
import smartcity.lights.LightColor;
import smartcity.lights.abstractions.ICrossroad;
import smartcity.lights.abstractions.ICrossroadFactory;
import utilities.Siblings;

import java.util.List;

public class CrossroadFactory implements ICrossroadFactory {
    private final EventBus eventBus;
    private final ICrossroadParser crossroadParser;
    private final ITroublePointsConfigContainer configContainer;

    @Inject
    public CrossroadFactory(EventBus eventBus,
                            ICrossroadParser crossroadParser,
                            ITroublePointsConfigContainer configContainer) {
        this.eventBus = eventBus;
        this.crossroadParser = crossroadParser;
        this.configContainer = configContainer;
    }

    @Override
    public ICrossroad create(int managerId, Node crossroad) {
        var lightGroups = getLightGroups(crossroad);
        return create(managerId, lightGroups);
    }

    @Override
    public ICrossroad create(int managerId, OSMNode centerCrossroadNode) {
        var lightGroups = getLightGroups(centerCrossroadNode);
        return create(managerId, lightGroups);
    }

    private ICrossroad create(int managerId, Siblings<SimpleLightGroup> lightGroups) {
        return new SimpleCrossroad(eventBus, configContainer, managerId, lightGroups);
    }

    private Siblings<SimpleLightGroup> getLightGroups(Node crossroad) {
        var lightGroups = crossroadParser.getLightGroups(crossroad);
        return getLightGroups(lightGroups.first, lightGroups.second);
    }

    private Siblings<SimpleLightGroup> getLightGroups(List<LightInfo> groupA, List<LightInfo> groupB) {
        var lightGroupA = new SimpleLightGroup(LightColor.RED, groupA);
        var lightGroupB = new SimpleLightGroup(LightColor.GREEN, groupB);

        return Siblings.of(lightGroupA, lightGroupB);
    }

    private Siblings<SimpleLightGroup> getLightGroups(OSMNode centerCrossroadNode) {
        var lightGroups = crossroadParser.getLightGroups(centerCrossroadNode);
        return getLightGroups(lightGroups.first, lightGroups.second);
    }
}
