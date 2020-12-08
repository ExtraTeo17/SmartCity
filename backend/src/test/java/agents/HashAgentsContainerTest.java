package agents;

import agents.abstractions.AbstractAgent;
import agents.abstractions.IAgentsContainer;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import mocks.ContainerControllerMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import osmproxy.elements.OSMStation;
import routing.abstractions.IRouteGenerator;
import routing.abstractions.IRouteTransformer;
import smartcity.config.ConfigContainer;
import smartcity.config.abstractions.IChangeTransportConfigContainer;
import smartcity.lights.abstractions.ICrossroad;
import smartcity.stations.StationStrategy;
import smartcity.task.abstractions.ITaskProvider;
import vehicles.Bus;
import vehicles.MovingObject;
import vehicles.Pedestrian;
import vehicles.enums.VehicleType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static mocks.TestInstanceCreator.createEventBus;
import static mocks.TestInstanceCreator.createTimeProvider;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("OverlyCoupledClass")
class HashAgentsContainerTest {
    private final Random random = new Random();
    private ContainerController controller;
    private IAgentsContainer agentsContainer;
    private IdGenerator idGenerator;

    @BeforeEach
    void setUp() {
        this.controller = new ContainerControllerMock();
        this.agentsContainer = new HashAgentsContainer(this.controller);
        this.idGenerator = new IdGenerator();
    }

    @Test
    void tryAdd_allRegistered_shouldAddAll() {
        // Arrange
        random.setSeed(40);
        agentsContainer.registerAll(AgentsModule.agentTypes);
        idGenerator.registerAll(AgentsModule.agentTypes);

        int agentsPerTypeCount = 10;
        List<AbstractAgent> agents = new ArrayList<>(agentsPerTypeCount);

        var mockCrossroad = mock(ICrossroad.class);
        var mockVehicle = mock(MovingObject.class);
        Mockito.when(mockVehicle.getVehicleType()).thenReturn("car");

        for (int i = 0; i < agentsPerTypeCount; ++i) {
            agents.add(getPedestrianAgent());
            agents.add(getBusAgent());
            agents.add(getStationAgent());
            agents.add(getCarAgent());
            agents.add(getBikeAgent());
            agents.add(getLightManagerAgent());
        }
        int agentTypes = agents.size() / agentsPerTypeCount;

        // Act & Assert
        for (int i = 0; i < agents.size(); i += agentTypes) {
            int begIndex = i + random.nextInt(agentTypes);
            int currIndex = begIndex;

            // Helps to randomize order of adding agents
            do {
                var agent = agents.get(currIndex);
                assertTrue(agentsContainer.tryAdd(agent), "Should add agent: " + agent.getPredictedName());
                currIndex = rotateIndex(currIndex, i, i + agentTypes);
            } while (currIndex != begIndex);
        }
    }

    private PedestrianAgent getPedestrianAgent() {
        var ped = mock(Pedestrian.class);
        when(ped.getVehicleType()).thenReturn(VehicleType.PEDESTRIAN.toString());
        return new PedestrianAgent(idGenerator.get(PedestrianAgent.class), ped,
                createTimeProvider(), mock(ITaskProvider.class), createEventBus(), mock(IRouteGenerator.class),
                mock(IChangeTransportConfigContainer.class));
    }

    private BikeAgent getBikeAgent() {
        var mov = mock(MovingObject.class);
        when(mov.getVehicleType()).thenReturn(VehicleType.BIKE.toString());
        return new BikeAgent(idGenerator.get(BikeAgent.class), mov,
                createTimeProvider(), createEventBus());
    }

    private CarAgent getCarAgent() {
        var mov = mock(MovingObject.class);
        when(mov.getVehicleType()).thenReturn(VehicleType.REGULAR_CAR.toString());
        return new CarAgent(idGenerator.get(CarAgent.class), mov,
                createTimeProvider(), mock(IRouteGenerator.class), mock(IRouteTransformer.class),
                createEventBus(), mock(ConfigContainer.class));
    }

    private StationAgent getStationAgent() {
        return new StationAgent(idGenerator.get(StationAgent.class), mock(OSMStation.class),
                mock(StationStrategy.class), createTimeProvider(), createEventBus());
    }

    private LightManagerAgent getLightManagerAgent() {
        return new LightManagerAgent(idGenerator.get(LightManagerAgent.class), mock(ICrossroad.class),
                createTimeProvider(), createEventBus(), mock(ConfigContainer.class));
    }

    private BusAgent getBusAgent() {
        return new BusAgent(idGenerator.get(BusAgent.class), mock(Bus.class),
                createTimeProvider(), createEventBus(), mock(ConfigContainer.class));
    }


    private int rotateIndex(int index, int start, int exclusiveEnd) {
        ++index;
        if (index == exclusiveEnd) {
            return start;
        }
        return index;
    }

    @AfterEach
    void cleanUp() {
        try {
            this.controller.kill();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}