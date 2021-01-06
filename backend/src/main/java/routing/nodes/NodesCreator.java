package routing.nodes;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import events.LightManagersReadyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import routing.abstractions.INodesContainer;

public class NodesCreator {
    private static final Logger logger = LoggerFactory.getLogger(NodesCreator.class);
    private final INodesContainer nodesContainer;

    @Inject
    public NodesCreator(INodesContainer nodesContainer) {this.nodesContainer = nodesContainer;}

    @Subscribe
    void handle(LightManagersReadyEvent e) {
        logger.info("Handling LightManagersReadyEvent: " + e.lightManagers.size());
        for (var manager : e.lightManagers) {
            for (var light : manager.getLights()) {
                nodesContainer.addLightManagerNode(LightManagerNode.of(light, manager.getId()));
            }
        }
    }
}
