package agents;

import agents.abstractions.IAgentsContainer;
import agents.abstractions.IAgentsFactory;
import com.google.common.eventbus.EventBus;
import mocks.ContainerControllerMock;
import org.junit.jupiter.api.Test;
import osmproxy.LightAccessManager;
import osmproxy.abstractions.ILightAccessManager;
import osmproxy.abstractions.IMapAccessManager;
import osmproxy.buses.BusLinesManager;
import routing.NodesContainer;
import routing.RoutingConstants;
import routing.abstractions.IRouteGenerator;
import routing.abstractions.IRouteTransformer;
import routing.core.IGeoPosition;
import routing.core.IZone;
import routing.core.Zone;
import smartcity.ITimeProvider;
import smartcity.config.ConfigContainer;
import smartcity.config.StaticConfig;
import smartcity.lights.abstractions.ICrossroadFactory;
import smartcity.lights.core.CrossroadFactory;
import smartcity.lights.core.CrossroadParser;
import testutils.FileLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("OverlyCoupledClass")
class AgentsCreatorTests {
    private final IZone defaultZone = Zone.of(52.23682, 21.01681, 600);

    @Test
    void tryConstructLightManagers_defaultCarZone() {
        if (StaticConfig.USE_DEPRECATED_XML_FOR_LIGHT_MANAGERS) {
            fail("Test is designed for new solution");
        }

        // Arrange
        var agentsContainer = new HashAgentsContainer(new ContainerControllerMock());
        agentsContainer.register(LightManagerAgent.class);
        var creator = setupAgentsCreator(agentsContainer);

        // Act
        var success = creator.tryConstructLightManagers();

        // Assert
        assertTrue(success);
        assertEquals(5, agentsContainer.size(LightManagerAgent.class));

        var context = new Object() {
            private final List<IGeoPosition> allPositions = new ArrayList<>();
        };
        agentsContainer.forEach(LightManagerAgent.class, manager -> {
            var positions = manager.getLights();
            assertTrue(positions.size() > 2);
            context.allPositions.addAll(positions);
        });

        var allPositions = context.allPositions;
        assertEquals(16, allPositions.size());
        for (int i = 0; i < 16; ++i) {
            var posA = allPositions.get(i);
            for (int j = i + 1; j < 16; ++j) {
                var posB = allPositions.get(j);
                assertTrue(posA.compareTo(posB) != 0, "No 2 positions of lights should be equal");
                var distance = posA.distance(posB) * RoutingConstants.METERS_PER_DEGREE;
                assertTrue(distance > 5, "Distance between each light should be at least 5 meters\n" +
                        "  distance:" + distance);
            }
        }
    }

    private AgentsCreator setupAgentsCreator(IAgentsContainer agentsContainer) {
        var lightAccessManager = setupLightAccessManager();
        var configContainer = mock(ConfigContainer.class);
        var busLinesManager = mock(BusLinesManager.class);
        var agentsFactory = setupAgentsFactory();
        var eventBus = new EventBus();
        var mapAccessManager = mock(IMapAccessManager.class);
        var routeGenerator = mock(IRouteGenerator.class);
        var osmContainer = mock(NodesContainer.class);

        return new AgentsCreator(agentsContainer, configContainer, busLinesManager, agentsFactory,
                eventBus, lightAccessManager, mapAccessManager, routeGenerator);
    }

    private ILightAccessManager setupLightAccessManager() {
        var mapAccessManager = mock(IMapAccessManager.class);
        var lightsFile = FileLoader.getDocument("DefaultCarZoneLights.xml");
        when(mapAccessManager.getNodesDocument(any())).thenReturn(Optional.of(lightsFile));
        return new LightAccessManager(mapAccessManager, defaultZone);
    }

    private IAgentsFactory setupAgentsFactory() {
        var idGenerator = new IdGenerator();
        idGenerator.register(LightManagerAgent.class);
        var timeProvider = mock(ITimeProvider.class);
        var routeTransformer = mock(IRouteTransformer.class);
        var crossroadFactory = setupCrossroadFactory();

        return new AgentsFactory(idGenerator, timeProvider, routeTransformer, crossroadFactory);
    }

    private ICrossroadFactory setupCrossroadFactory() {
        var eventBus = new EventBus();
        var crossroadParser = new CrossroadParser();

        return new CrossroadFactory(eventBus, crossroadParser);
    }
}