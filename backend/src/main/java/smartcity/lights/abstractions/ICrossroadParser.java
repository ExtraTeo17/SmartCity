package smartcity.lights.abstractions;

import org.w3c.dom.Node;
import osmproxy.elements.OSMNode;
import smartcity.lights.core.data.LightInfo;
import utilities.Siblings;

import java.util.List;
//TODO:dokumentacja

public interface ICrossroadParser {
    Siblings<List<LightInfo>> getLightGroups(OSMNode crossroadCenter);

    Siblings<List<LightInfo>> getLightGroups(Node crossroad);
}
