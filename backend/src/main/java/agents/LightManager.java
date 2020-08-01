package agents;

import lightstrategies.LightManagerStrategy;
import lightstrategies.LightStrategy;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import osmproxy.elements.OSMNode;

import java.util.List;

public class LightManager extends AbstractAgent {
    private static final Logger logger = LoggerFactory.getLogger(LightManager.class);
    private final LightStrategy strategy;

    @Override
    public String getNamePrefix() {
        return "LightManager";
    }

    public LightManager(Node crossroad, int id) {
        super(id);
        strategy = new LightManagerStrategy(crossroad, id);
    }

    public LightManager(OSMNode centerCrossroadNode, int id) {
        super(id);
        strategy = new LightManagerStrategy(centerCrossroadNode, id);
    }

    @Override
    protected void setup() {
        print("I'm a traffic manager.");
        // TODO: Remove strategy initialization from this class
        //  Inject it as dependency or better add factory for lightManager with parameter
        strategy.applyStrategy(this);
    }

    public void print(String message) {
        logger.info(getLocalName() + ": " + message);
    }

    public void draw(List<Painter<JXMapViewer>> waypointPainter) {
        strategy.drawCrossroad(waypointPainter);
    }
}
