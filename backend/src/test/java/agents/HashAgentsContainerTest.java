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
import smartcity.lights.abstractions.ICrossroad;
import vehicles.MovingObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

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

        var mockCrossroad = Mockito.mock(ICrossroad.class);
        var mockVehicle = Mockito.mock(MovingObject.class);
        Mockito.when(mockVehicle.getVehicleType()).thenReturn("car");

        for (int i = 0; i < agentsPerTypeCount; ++i) {
            // TODO: Mocks, not null
            agents.add(new PedestrianAgent(idGenerator.get(PedestrianAgent.class), null, null));
            agents.add(new BusAgent(idGenerator.get(BusAgent.class), null, null));
            agents.add(new StationAgent(idGenerator.get(StationAgent.class), null, null, null));
            agents.add(new VehicleAgent(idGenerator.get(VehicleAgent.class), mockVehicle, null));
            agents.add(new LightManagerAgent(idGenerator.get(LightManagerAgent.class), null, mockCrossroad));
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