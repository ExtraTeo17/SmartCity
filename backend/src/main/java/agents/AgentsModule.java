package agents;

import agents.abstractions.IAgentsContainer;
import agents.abstractions.IAgentsFactory;
import agents.singletons.SingletonAgentsActivator;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import genesis.AbstractModule;
import jade.wrapper.ContainerController;

public class AgentsModule extends AbstractModule {
    public static final Class<?>[] agentTypes = {
            CarAgent.class,
            BusAgent.class,
            LightManagerAgent.class,
            StationAgent.class,
            PedestrianAgent.class,
            BikeAgent.class
    };

    @Override
    public void configure(Binder binder) {
        super.configure(binder);
        binder.bind(IAgentsFactory.class).to(AgentsFactory.class).in(Singleton.class);
        binder.bind(AgentsPreparer.class).asEagerSingleton();
        binder.bind(TroubleManagerAgent.class).in(Singleton.class);
        binder.bind(SingletonAgentsActivator.class).asEagerSingleton();
    }

    @Provides
    @Singleton
    @Inject
    IAgentsContainer getAgentsContainer(ContainerController controller) {
        var container = new HashAgentsContainer(controller);
        container.registerAll(agentTypes);
        return container;
    }

    @Provides
    @Singleton
    IdGenerator getIdGenerator() {
        var generator = new IdGenerator();
        generator.registerAll(agentTypes);
        return generator;
    }
}
