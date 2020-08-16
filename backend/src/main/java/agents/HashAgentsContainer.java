package agents;

import agents.abstractions.IAgentsContainer;
import com.google.inject.Inject;
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

class HashAgentsContainer<TAgent extends AbstractAgent>
        implements IAgentsContainer<TAgent> {
    private final static Logger logger = LoggerFactory.getLogger(HashAgentsContainer.class);
    private final ContainerController controller;
    private final Map<Class<?>, HashSet<TAgent>> container;

    @Inject
    HashAgentsContainer(ContainerController controller) {
        this.controller = controller;
        this.container = new ConcurrentHashMap<>();
    }

    @Override
    public boolean tryAdd(@NotNull TAgent agent) {
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
    public <TSpec extends TAgent> Iterator<TSpec> iterator(Class<TSpec> type) {
        var set = container.get(type);
        if (set == null) {
            throw new NotRegisteredException(type);
        }

        return (Iterator<TSpec>) set.iterator();
    }


    private boolean tryAddAgent(TAgent agent) {
        try {
            controller.acceptNewAgent(agent.getPredictedName(), agent);
        } catch (StaleProxyException e) {
            logger.warn("Error adding agent");
            return false;
        }

        return true;
    }

    @Override
    public boolean contains(TAgent agent) {
        var type = agent.getClass();
        var set = container.get(type);
        if (set == null) {
            throw new NotRegisteredException(type);
        }

        return set.contains(agent);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <TSpec extends TAgent> void removeIf(Class<TSpec> type, Predicate<TSpec> predicate) {
        var set = container.get(type);
        if (set == null) {
            throw new NotRegisteredException(type);
        }

        set.removeIf(tAgent -> predicate.test((TSpec) tAgent));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <TSpec extends TAgent> void forEach(Class<TSpec> type, Consumer<TSpec> consumer) {
        var set = container.get(type);
        if (set == null) {
            throw new NotRegisteredException(type);
        }


        set.forEach(tAgent -> consumer.accept((TSpec) tAgent));
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
