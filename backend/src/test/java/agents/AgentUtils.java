package agents;

import agents.abstractions.IAgentsContainer;
import agents.abstractions.IAgentsFactory;
import com.google.common.eventbus.EventBus;
import osmproxy.LightAccessManager;
import osmproxy.abstractions.ICacheWrapper;
import osmproxy.abstractions.ILightAccessManager;
import osmproxy.abstractions.IMapAccessManager;
import osmproxy.buses.BusLinesManager;
import routing.abstractions.IRouteGenerator;
import routing.abstractions.IRouteTransformer;
import routing.core.IZone;
import routing.core.Zone;
import routing.nodes.NodesContainer;
import smartcity.ITimeProvider;
import smartcity.config.ConfigContainer;
import smartcity.config.ConfigMutator;
import smartcity.lights.abstractions.ICrossroadFactory;
import smartcity.lights.core.CrossroadFactory;
import smartcity.lights.core.CrossroadParser;
import smartcity.task.TaskProvider;
import testutils.FileLoader;
import testutils.ReflectionHelper;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("OverlyCoupledClass")
class AgentUtils {
    private static final IZone defaultZone = Zone.of(52.23682, 21.01681, 600);
    private static final IdGenerator idGenerator = new IdGenerator();

    private static IAgentsFactory setupAgentsFactory() {
        var idGenerator = new IdGenerator();
        idGenerator.register(LightManagerAgent.class);
        var timeProvider = mock(ITimeProvider.class);
        var routeTransformer = mock(IRouteTransformer.class);
        var crossroadFactory = setupCrossroadFactory();
        var eventBus = new EventBus();

        return new AgentsFactory(idGenerator, eventBus, timeProvider, routeTransformer,
                crossroadFactory, mock(IRouteGenerator.class), mock(ConfigContainer.class), mock(TaskProvider.class));
    }

    private static ICrossroadFactory setupCrossroadFactory() {
        var eventBus = new EventBus();
        var crossroadParser = new CrossroadParser();
        ReflectionHelper.setStatic("counter", ConfigMutator.class, 0);
        var configContainer = new ConfigContainer();

        return new CrossroadFactory(eventBus, crossroadParser, configContainer);
    }

    static AgentsPreparer setupAgentsCreator(IAgentsContainer agentsContainer) {
        var lightAccessManager = setupLightAccessManager();
        var configContainer = mock(ConfigContainer.class);
        var busLinesManager = mock(BusLinesManager.class);
        var agentsFactory = setupAgentsFactory();
        var eventBus = new EventBus();
        var mapAccessManager = mock(IMapAccessManager.class);
        var routeGenerator = mock(IRouteGenerator.class);
        var osmContainer = mock(NodesContainer.class);
        var cacheWrapper = mock(ICacheWrapper.class);

        return new AgentsPreparer(agentsContainer, configContainer, busLinesManager, agentsFactory,
                eventBus, lightAccessManager, mapAccessManager, routeGenerator, cacheWrapper);
    }

    private static ILightAccessManager setupLightAccessManager() {
        var mapAccessManager = mock(IMapAccessManager.class);
        var lightsFile = FileLoader.getDocument("DefaultCarZoneLights.xml");
        when(mapAccessManager.getNodesDocument(any())).thenReturn(Optional.of(lightsFile));
        return new LightAccessManager(mapAccessManager, defaultZone);
    }

}
