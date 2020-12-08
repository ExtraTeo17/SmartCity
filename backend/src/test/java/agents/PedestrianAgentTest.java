package agents;

/*
public class PedestrianAgentTest {
    @Test
    void pedestrian_is_troubled_shouldNotMove() {
        // Arrange

      var agentUtills = new AgentUtills();
        var time = LocalDateTime.of(2020, 10, 12, 10, 10);
        var timeProvider = mock(ITimeProvider.class);
        var taskProvider = mock(ITaskProvider.class);
        var pedestrian = createPedestrian(timeProvider, taskProvider);

        var agentsContainer = new HashAgentsContainer(new ContainerControllerMock());
        agentsContainer.register(LightManagerAgent.class);

        var agentHelper = new AgentUtills();
        var creator = agentHelper.setupAgentsCreator(agentsContainer);
        PedestrianAgent agent = new PedestrianAgent(1, pedestrian,
                createTimeProvider(), mock(ITaskProvider.class), createEventBus(), mock(IRouteGenerator.class),
                mock(ITroublePointsConfigContainer.class));
        BusManagerAgent busManager = new BusManagerAgent(
                createTimeProvider(),  createEventBus(), new HashSet<>());


        agentsContainer.tryAdd(agent);
        agentsContainer.tryAdd(busManager);


        var moveIndexBeforeTrouble = pedestrian.getMoveIndex();
        // Act
        pedestrian.setTroubled(false);
        agent.setup();
        // Assert
        var moveIndexAfterTrouble = pedestrian.getMoveIndex();
        assertEquals(moveIndexBeforeTrouble, moveIndexAfterTrouble);

    }
    private Pedestrian createPedestrian(ITimeProvider timeProvider, ITaskProvider taskProvider) {
        return new Pedestrian(timeProvider);
    }
}*/
