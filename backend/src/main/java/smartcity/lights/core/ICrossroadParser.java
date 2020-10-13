package smartcity.lights.core;

import org.w3c.dom.Node;
import osmproxy.elements.OSMNode;
import utilities.Siblings;

import java.util.List;

public interface ICrossroadParser {
    Siblings<List<LightInfo>> getLightGroups(OSMNode crossroadCenter);

    Siblings<List<LightInfo>> getLightGroups(Node crossroad);
}
