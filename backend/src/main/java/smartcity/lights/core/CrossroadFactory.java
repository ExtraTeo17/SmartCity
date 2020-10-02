package smartcity.lights.core;

import agents.utilities.LightColor;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import org.w3c.dom.Node;
import osmproxy.elements.OSMNode;
import smartcity.lights.abstractions.ICrossroad;
import smartcity.lights.abstractions.ICrossroadFactory;
import utilities.Siblings;

public class CrossroadFactory implements ICrossroadFactory {
    private final EventBus eventBus;

    @Inject
    public CrossroadFactory(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public ICrossroad create(Siblings<SimpleLightGroup> lightGroups) {
        return new SimpleCrossroad(eventBus, lightGroups);
    }

    @Override
    public ICrossroad create(Node crossroad, int managerId) {
        var lightGroups = getLightGroups(crossroad, managerId);
        return create(lightGroups);
    }

    @Override
    public ICrossroad create(OSMNode centerCrossroadNode, int managerId) {
        var lightGroups = getLightGroups(centerCrossroadNode, managerId);
        return create(lightGroups);
    }

    private static Siblings<SimpleLightGroup> getLightGroups(Node crossroad, int managerId) {
        var crossroadChildren = crossroad.getChildNodes();
        var lightGroupA = new SimpleLightGroup(crossroadChildren.item(1), LightColor.RED, managerId);
        var lightGroupB = new SimpleLightGroup(crossroadChildren.item(3), LightColor.GREEN, managerId);

        return Siblings.of(lightGroupA, lightGroupB);
    }

    private static Siblings<SimpleLightGroup> getLightGroups(OSMNode centerCrossroadNode, int managerId) {
        var info = new CrossroadInfo(centerCrossroadNode);
        var lightGroupA = new SimpleLightGroup(info.getFirstLightGroupInfo(), LightColor.RED, managerId);
        var lightGroupB = new SimpleLightGroup(info.getSecondLightGroupInfo(), LightColor.GREEN, managerId);

        return Siblings.of(lightGroupA, lightGroupB);
    }
}
