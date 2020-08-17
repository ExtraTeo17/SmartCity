package agents;

import agents.abstractions.IAgentsContainer;
import com.google.inject.Inject;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

class HashAgentsContainer implements IAgentsContainer {
    private final static Logger logger = LoggerFactory.getLogger(HashAgentsContainer.class);
    private final ContainerController controller;
    private final Map<Class<?>, Map<Integer, AbstractAgent>> container;

    @Inject
    HashAgentsContainer(ContainerController controller) {
        this.controller = controller;
        this.container = new ConcurrentHashMap<>();
    }

    @Override
    public boolean tryAdd(@NotNull AbstractAgent agent) {
        var type = agent.getClass();
        var collection = container.get(type);
        if (collection == null) {
            throw new NotRegisteredException(type);
        }

        if (tryAddAgent(agent)) {
            return collection.putIfAbsent(agent.getId(), agent) == null;
        }

        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <TAgent extends AbstractAgent> Iterator<TAgent> iterator(Class<TAgent> type) {
        var collection = container.get(type);
        if (collection == null) {
            throw new NotRegisteredException(type);
        }

        return (Iterator<TAgent>) collection.values().iterator();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <TAgent extends AbstractAgent> Stream<TAgent> stream(Class<TAgent> type) {
        var collection = container.get(type);
        if (collection == null) {
            throw new NotRegisteredException(type);
        }

        return (Stream<TAgent>) collection.values().stream();
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
    public boolean contains(AbstractAgent agent) {
        var type = agent.getClass();
        var collection = container.get(type);
        if (collection == null) {
            throw new NotRegisteredException(type);
        }

        return collection.containsKey(agent.getId());
    }

    @Override
    public <TAgent extends AbstractAgent> Optional<TAgent> get(Class<TAgent> type, Predicate<TAgent> predicate) {
        var collection = container.get(type);
        if (collection == null) {
            throw new NotRegisteredException(type);
        }

        return collection.values().stream()
                .filter(abstractAgent -> predicate.test(type.cast(abstractAgent))).map(type::cast)
                .findFirst();
    }

    @Override
    public <TAgent extends AbstractAgent> boolean remove(TAgent agent) {
        return remove(agent.getClass(), agent.getId()).isPresent();
    }

    @Override
    public <TAgent extends AbstractAgent> Optional<TAgent> remove(Class<TAgent> type, int agentId) {
        var collection = container.get(type);
        if (collection == null) {
            throw new NotRegisteredException(type);
        }

        var agent = collection.remove(agentId);
        return agent != null ? Optional.of(type.cast(agent)) : Optional.empty();
    }

    @Override
    public <TAgent extends AbstractAgent> void removeIf(Class<TAgent> type, Predicate<TAgent> predicate) {
        var collection = container.get(type);
        if (collection == null) {
            throw new NotRegisteredException(type);
        }

        collection.entrySet().removeIf(tAgent -> predicate.test(type.cast(tAgent)));
    }

    @Override
    public <TAgent extends AbstractAgent> void forEach(Class<TAgent> type, Consumer<TAgent> consumer) {
        var collection = container.get(type);
        if (collection == null) {
            throw new NotRegisteredException(type);
        }

        collection.forEach((key, tAgent) -> consumer.accept(type.cast(tAgent)));
    }


    @Override
    public int size(Class<?> type) {
        var collection = container.get(type);
        if (collection == null) {
            throw new NotRegisteredException(type);
        }

        return collection.size();
    }

    @Override
    public void clear(Class<?> type) {
        var collection = container.get(type);
        if (collection == null) {
            throw new NotRegisteredException(type);
        }

        collection.clear();
    }

    @Override
    public final void register(Class<?>... types) {
        for (var type : types) {
            container.put(type, new HashMap<>());
        }
    }

    @Override
    public void registerAll(Class<?>[] types) {
        for (var type : types) {
            container.put(type, new HashMap<>());
        }
    }
}
