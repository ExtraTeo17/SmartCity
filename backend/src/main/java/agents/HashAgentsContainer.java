package agents;

import agents.abstractions.AbstractAgent;
import agents.abstractions.IAgentsContainer;
import com.google.inject.Inject;
import jade.wrapper.AgentController;
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
    private static final Logger logger = LoggerFactory.getLogger(HashAgentsContainer.class);
    private final ContainerController controller;
    private final Map<Class<?>, Map<Integer, AbstractAgent>> container;

    @Inject
    HashAgentsContainer(ContainerController controller) {
        this.controller = controller;
        this.container = new ConcurrentHashMap<>();
    }

    @Override
    public boolean tryAdd(@NotNull AbstractAgent agent, boolean shouldTryAccept) {
        var type = agent.getClass();
        var collection = getOrThrow(type);

        if (shouldTryAccept) {
            if (tryAccept(agent)) {
                return collection.putIfAbsent(agent.getId(), agent) == null;
            }
        }
        else {
            return collection.putIfAbsent(agent.getId(), agent) == null;
        }

        return false;
    }

    @Override
    public boolean tryAccept(@NotNull AbstractAgent agent) {
        AgentController agentController;
        try {
            agentController = controller.acceptNewAgent(agent.getPredictedName(), agent);
        } catch (StaleProxyException e) {
            logger.warn("Error adding agent", e);
            return false;
        }

        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <TAgent extends AbstractAgent> Iterator<TAgent> iterator(Class<TAgent> type) {
        return (Iterator<TAgent>) getOrThrow(type).values().iterator();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <TAgent extends AbstractAgent> Stream<TAgent> stream(Class<TAgent> type) {
        return (Stream<TAgent>) getOrThrow(type).values().stream();
    }

    @Override
    public boolean contains(AbstractAgent agent) {
        var type = agent.getClass();
        return getOrThrow(type).containsKey(agent.getId());
    }

    @Override
    public <TAgent extends AbstractAgent> Optional<TAgent> get(Class<TAgent> type, Predicate<TAgent> predicate) {
        return getOrThrow(type).values().stream().map(type::cast)
                .filter(predicate)
                .findFirst();
    }

    @Override
    public <TAgent extends AbstractAgent> Optional<TAgent> getRandom(Class<TAgent> type, Random random) {
        var collection = getOrThrow(type).values();
        int ind = random.nextInt(collection.size());
        return collection.stream().map(type::cast)
                .skip(ind)
                .findFirst();
    }

    @Override
    public <TAgent extends AbstractAgent> boolean remove(TAgent agent) {
        return remove(agent.getClass(), agent.getId()).isPresent();
    }

    @Override
    public <TAgent extends AbstractAgent> Optional<TAgent> remove(Class<TAgent> type, int agentId) {
        var agent = getOrThrow(type).remove(agentId);
        if (agent != null) {
            tryKillAgent(agent);
            return Optional.of(type.cast(agent));
        }

        return Optional.empty();
    }

    @Override
    public <TAgent extends AbstractAgent> void forEach(Class<TAgent> type, Consumer<TAgent> consumer) {
        getOrThrow(type).forEach((key, tAgent) -> consumer.accept(type.cast(tAgent)));
    }

    @Override
    public int size(Class<?> type) {
        return getOrThrow(type).size();
    }

    @Override
    public synchronized void clear(Class<?> type) {
        var collection = getOrThrow(type);
        tryDeleteAll(collection.values());
    }

    @Override
    public synchronized void clearAll() {
        for (var collection : container.values()) {
            tryDeleteAll(collection.values());
        }
    }

    private void tryDeleteAll(Collection<AbstractAgent> agents) {
        for (var agent : agents) {
            tryKillAgent(agent);
        }
        agents.clear();
    }

    private void tryKillAgent(AbstractAgent agent) {
        try {
            if (agent.isAlive()) {
                agent.doDelete();
            }
        } catch (Exception e) {
            logger.warn("Failed to stop agent execution:" + agent.getLocalName(), e);
        }
    }

    private Map<Integer, AbstractAgent> getOrThrow(Class<?> type) {
        var collection = container.get(type);
        if (collection == null) {
            throw new NotRegisteredException(type);
        }

        return collection;
    }

    @Override
    public void registerAll(Class<?>[] types) {
        for (var type : types) {
            container.put(type, new ConcurrentHashMap<>());
        }
    }
}
