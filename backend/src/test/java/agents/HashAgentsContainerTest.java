package agents;

import agents.abstractions.AbstractAgent;
import agents.abstractions.IAgentsContainer;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import mocks.ContainerControllerMock;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import osmproxy.elements.OSMNode;
import vehicles.MovingObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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

        var mockNode = Mockito.mock(OSMNode.class);
        Mockito.when(mockNode.getParentWaysIterator()).thenReturn(Collections.emptyIterator());
        var mockVehicle = Mockito.mock(MovingObject.class);
        Mockito.when(mockVehicle.getVehicleType()).thenReturn("car");

        for (int i = 0; i < agentsPerTypeCount; ++i) {
            // TODO: Mocks, not null
            agents.add(new PedestrianAgent(idGenerator.get(PedestrianAgent.class), null));
            agents.add(new BusAgent(idGenerator.get(BusAgent.class), null, null));
            agents.add(new StationAgent(idGenerator.get(StationAgent.class), null));
            agents.add(new VehicleAgent(idGenerator.get(VehicleAgent.class), mockVehicle));
            agents.add(new LightManagerAgent(idGenerator.get(LightManagerAgent.class), mockNode));
        }
        int agentTypes = agents.size() / agentsPerTypeCount;

        // Act & Assert
        for (int i = 0; i < agents.size(); i += agentTypes) {
            int begIndex = i + random.nextInt(agentTypes);
            int currIndex = begIndex;

            // Helps to randomize order of adding agents
            do {
                Assert.assertTrue(agentsContainer.tryAdd(agents.get(currIndex)));
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