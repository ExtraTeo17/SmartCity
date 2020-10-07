package agents;

import lightstrategies.LightManagerStrategy;
import lightstrategies.LightStrategy;
import org.jxmapviewer.viewer.DefaultWaypointRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import osmproxy.elements.OSMNode;
import jade.core.Agent;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.w3c.dom.Node;

import java.util.List;

public class LightManager extends Agent {
    private static final Logger logger = LoggerFactory.getLogger(LightManager.class);
    private final LightStrategy strategy;
    private final long agentId;

    public LightManager(Node crossroad, Long id) {
        agentId = id;
        strategy = new LightManagerStrategy(crossroad, id);
    }

    public LightManager(OSMNode centerCrossroadNode, long id) {
        agentId = id;
        strategy = new LightManagerStrategy(centerCrossroadNode, id);
    }

    protected void setup() {
        print("I'm a traffic manager.");
        strategy.ApplyStrategy(this);
    }

    public void takeDown() {
        super.takeDown();
    }

    public void print(String message) {
        logger.info(getLocalName() + ": " + message);
    }

    public void draw(List<Painter<JXMapViewer>> waypointPainter) {
        strategy.drawCrossroad(waypointPainter);
    }

    public String getId() {
        return Long.toString(agentId);
    }
}
