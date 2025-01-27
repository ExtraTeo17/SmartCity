package smartcity.lights.abstractions;

import org.w3c.dom.Node;
import osmproxy.elements.OSMNode;

/**
 * Create crossroads
 */
public interface ICrossroadFactory {
    ICrossroad create(int managerId, Node crossroad);

    ICrossroad create(int managerId, OSMNode centerCrossroadNode);
}
