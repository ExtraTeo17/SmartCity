package agents.abstractions;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Custom container for agents which provides methods for maintaining them.
 */
public interface IAgentsContainer extends IRegistrable {

	/**
	 * Perform an attempt to add agent to the container.
	 *
	 * @param agent The agent to be added to this container
	 * @param shouldTryAccept Whether the agent should also be accepted
	 * by the agent controller.
	 * @return true if the operation was sucessful, false otherwise
	 */
    boolean tryAdd(@NotNull AbstractAgent agent, boolean shouldTryAccept);

    /**
     * Perform an attempt to add agent to the container.
     *
     * @param agent The agent to be added to this container
     * @return true if the operation was sucessful, false otherwise
     */
    default boolean tryAdd(@NotNull AbstractAgent agent) {
        return tryAdd(agent, true);
    }

    /**
     * Perform an attempt of accepting the agent by the agent controller.
     *
     * @param agent The agent to be accepted by the agent controller.
     * @return true if the operation was sucessful, false otherwise
     */
    boolean tryAccept(@NotNull AbstractAgent agent);

    default <TAgent extends AbstractAgent> Iterator<TAgent> iterator(Class<TAgent> type) {
        return stream(type).iterator();
    }

    <TAgent extends AbstractAgent> Stream<TAgent> stream(Class<TAgent> type);

    boolean contains(AbstractAgent agent);

    <TAgent extends AbstractAgent> Optional<TAgent> get(Class<TAgent> type, Predicate<TAgent> predicate);

    /**
     * Retrieve a random agent from this container.
     *
     * @param <TAgent> Type of the agent, extending {@link AbstractAgent}
     * @param type Class of the type of the agent
     * @param random Instance of pseudorandom numbers generator
     * @return Random agent from this container.
     */
    default <TAgent extends AbstractAgent> Optional<TAgent> getRandom(Class<TAgent> type, Random random) {
        int ind = random.nextInt(size(type));
        return stream(type)
                .skip(ind)
                .findFirst();
    }

    /**
     * Remove an agent from this container.
     *
     * @param <TAgent> Type of the agent, extending {@link AbstractAgent}
     * @param agent The agent to be removed from this container
     * @return true if the operation was sucessful, false otherwise
     */
    <TAgent extends AbstractAgent> boolean remove(TAgent agent);

    /**
     * Remove an agent from this container.
     *
     * @param <TAgent> Type of the agent, extending {@link AbstractAgent}
     * @param type Class of the type of the agent
     * @param agentId Identification number of the agent to be removed from
     * this container
     * @return true if the operation was sucessful, false otherwise
     */
    <TAgent extends AbstractAgent> Optional<TAgent> remove(Class<TAgent> type, int agentId);

    /**
     * Remove an agent from this container if a given predicate is satisfied.
     *
     * @param <TAgent> Type of the agent, extending {@link AbstractAgent}
     * @param type Class of the type of the agent
     * @param predicate The predicate which determines whether the agent shall be
     * removed from this container
     * @return true if the operation was sucessful, false otherwise
     */
    default <TAgent extends AbstractAgent> boolean removeIf(Class<TAgent> type, Predicate<TAgent> predicate) {
        AtomicBoolean result = new AtomicBoolean(true);
        forEach(type, agent -> {
            if (predicate.test(agent)) {
                result.set(result.get() && remove(agent));
            }
        });
        return result.get();
    }

    /**
     * Perform given operation on each of the agents in this container.
     *
     * @param <TAgent> Type of the agent, extending {@link AbstractAgent}
     * @param type Class of the type of the agent
     * @param consumer The operation to perform on the agents
     */
    default <TAgent extends AbstractAgent> void forEach(Class<TAgent> type, Consumer<TAgent> consumer) {
        stream(type).forEach(consumer);
    }

    int size(Class<?> type);

    void clear(Class<?> type);

    void clearAll();
}
