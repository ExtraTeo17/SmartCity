package agents;

import agents.abstractions.AbstractAgent;
import agents.abstractions.IAgentsContainer;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import mocks.ContainerControllerMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import osmproxy.elements.OSMStation;
import routing.abstractions.IRouteTransformer;
import smartcity.lights.abstractions.ICrossroad;
import smartcity.stations.StationStrategy;
import vehicles.Bus;
import vehicles.MovingObject;
import vehicles.Pedestrian;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static mocks.TestInstanceCreator.createEventBus;
import static mocks.TestInstanceCreator.createTimeProvider;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
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
            agents.add(getVehicleAgent());
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

    PedestrianAgent getPedestrianAgent() {
        return new PedestrianAgent(idGenerator.get(PedestrianAgent.class), mock(Pedestrian.class),
                createTimeProvider(), createEventBus());
    }

    VehicleAgent getVehicleAgent() {
        return new VehicleAgent(idGenerator.get(VehicleAgent.class), mock(MovingObject.class),
                createTimeProvider(), createEventBus(),5000,null);
    }

    StationAgent getStationAgent() {
        return new StationAgent(idGenerator.get(StationAgent.class), mock(OSMStation.class),
                mock(StationStrategy.class), createTimeProvider(), createEventBus());
    }

    LightManagerAgent getLightManagerAgent() {
        return new LightManagerAgent(idGenerator.get(LightManagerAgent.class), mock(ICrossroad.class),
                createTimeProvider(), createEventBus());
    }

    BusAgent getBusAgent() {
        return new BusAgent(idGenerator.get(BusAgent.class), mock(Bus.class),
                createTimeProvider(), createEventBus());
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