package routing;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import events.web.LightManagersReadyEvent;

public class NodesCreator {
    private final NodesContainer nodesContainer;

    @Inject
    public NodesCreator(NodesContainer nodesContainer) {this.nodesContainer = nodesContainer;}

    @Subscribe
    public void handle(LightManagersReadyEvent e) {
        for (var manager : e.lightManagers) {
            for (var light : manager.getLights()) {
                nodesContainer.addLightManagerNode(LightManagerNode.of(light, manager.getId()));
            }
        }
    }

}
