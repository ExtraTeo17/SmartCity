package agents;

import agents.abstractions.IAgentsContainer;
import com.google.inject.Inject;
import jade.core.Agent;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

class HashAgentsContainer implements IAgentsContainer {
    private final static Logger logger = LoggerFactory.getLogger(HashAgentsContainer.class);
    private final ContainerController controller;
    private final Map<Class<?>, HashSet<Agent>> container;

    @Inject
    HashAgentsContainer(ContainerController controller) {
        this.controller = controller;
        this.container = new ConcurrentHashMap<>();
    }

    @Override
    public boolean tryAdd(@NotNull AbstractAgent agent) {
        var type = agent.getClass();
        var set = container.get(type);
        if (set == null) {
            throw new NotRegisteredException(type);
        }

        if (tryAddAgent(agent)) {
            return set.add(agent);
        }

        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <TAgent extends Agent> Iterator<TAgent> iterator(Class<TAgent> type) {
        var set = container.get(type);
        if (set == null) {
            throw new NotRegisteredException(type);
        }

        return (Iterator<TAgent>) set.iterator();
    }


    private boolean tryAddAgent(@NotNull AbstractAgent agent) {
        try {
            controller.acceptNewAgent(agent.getPredictedName(), agent);
        } catch (StaleProxyException e) {
            logger.warn("Error adding agent");
            return false;
        }

        return true;
    }

    @Override
    public boolean contains(Agent agent) {
        var type = agent.getClass();
        var set = container.get(type);
        if (set == null) {
            throw new NotRegisteredException(type);
        }

        return set.contains(agent);
    }

    @Override
    public <TAgent extends Agent>  void removeIf(Class<TAgent> type, Predicate<TAgent> predicate) {
        var set = container.get(type);
        if (set == null) {
            throw new NotRegisteredException(type);
        }

        set.removeIf(tAgent -> predicate.test(type.cast(tAgent)));
    }

    @Override
    public <TAgent extends Agent> void forEach(Class<TAgent> type, Consumer<TAgent> consumer) {
        var set = container.get(type);
        if (set == null) {
            throw new NotRegisteredException(type);
        }


        set.forEach(tAgent -> consumer.accept(type.cast(tAgent)));
    }


    @Override
    public int size(Class<?> type) {
        var set = container.get(type);
        if (set == null) {
            throw new NotRegisteredException(type);
        }

        return set.size();
    }

    @Override
    public void clear(Class<?> type) {
        var set = container.get(type);
        if (set == null) {
            throw new NotRegisteredException(type);
        }

        set.clear();
    }

    @Override
    public final void register(Class<?>... types) {
        for (var type : types) {
            container.put(type, new HashSet<>());
        }
    }

    @Override
    public void registerAll(Class<?>[] types) {
        for (var type : types) {
            container.put(type, new HashSet<>());
        }
    }
}
