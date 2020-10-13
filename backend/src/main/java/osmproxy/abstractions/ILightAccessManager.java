package osmproxy.abstractions;

import osmproxy.elements.OSMNode;

import java.util.List;

public interface ILightAccessManager {
    List<OSMNode> getLightsOfTypeA();
}
