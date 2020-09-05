package agents;

import agents.abstractions.AbstractAgent;
import behaviourfactories.IBehaviourFactory;
import behaviourfactories.LightManagerBehaviourFactory;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import osmproxy.elements.OSMNode;
import routing.core.IGeoPosition;
import smartcity.lights.ICrossroad;
import smartcity.lights.SimpleCrossroad;

import java.util.List;

public class LightManagerAgent extends AbstractAgent {
    public static final String name = LightManagerAgent.class.getName().replace("Agent", "");
    private static final Logger logger = LoggerFactory.getLogger(LightManagerAgent.class);

    // TODO: Inject it as dependency
    private final IBehaviourFactory<LightManagerAgent> behaviourFactory;
    private final ICrossroad crossroad;

    public LightManagerAgent(int id, Node node) {
        super(id);
        behaviourFactory = new LightManagerBehaviourFactory();
        crossroad = new SimpleCrossroad(node, id);
    }

    public LightManagerAgent(int id, OSMNode centerCrossroadNode) {
        super(id);
        behaviourFactory = new LightManagerBehaviourFactory();
        crossroad = new SimpleCrossroad(centerCrossroadNode, id);
    }

    @Override
    public String getNamePrefix() {
        return name;
    }

    @Override
    protected void setup() {
        print("I'm a traffic manager.");
        crossroad.startLifetime();
        addBehaviour(behaviourFactory.createCyclicBehaviour(this));
        addBehaviour(behaviourFactory.createTickerBehaviour(this));
    }

    public ICrossroad getCrossroad() {
        return crossroad;
    }

    public List<IGeoPosition> getLightsPositions() {
        return crossroad.getLightsPositions();
    }

    public void print(String message) {
        logger.info(getLocalName() + ": " + message);
    }

    public void draw(List<Painter<JXMapViewer>> waypointPainter) {
        crossroad.draw(waypointPainter);
    }
}
