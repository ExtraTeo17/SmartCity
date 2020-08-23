package agents.abstractions;

import java.util.Iterator;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;


// I hate Java generics.
public interface IAgentsContainer extends IRegistrable {
    boolean tryAdd(AbstractAgent agent);

    default <TAgent extends AbstractAgent> Iterator<TAgent> iterator(Class<TAgent> type) {
        return stream(type).iterator();
    }

    <TAgent extends AbstractAgent> Stream<TAgent> stream(Class<TAgent> type);

    boolean contains(AbstractAgent agent);

    <TAgent extends AbstractAgent> Optional<TAgent> get(Class<TAgent> type, Predicate<TAgent> predicate);

    default <TAgent extends AbstractAgent> Optional<TAgent> getRandom(Class<TAgent> type, Random random) {
        int ind = random.nextInt(size(type));
        return stream(type)
                .skip(ind)
                .findFirst();
    }

    <TAgent extends AbstractAgent> boolean remove(TAgent agent);

    <TAgent extends AbstractAgent> Optional<TAgent> remove(Class<TAgent> type, int agentId);

    default <TAgent extends AbstractAgent> boolean removeIf(Class<TAgent> type, Predicate<TAgent> predicate) {
        AtomicBoolean result = new AtomicBoolean(true);
        forEach(type, agent -> {
            if (predicate.test(agent)) {
                result.set(result.get() && remove(agent));
            }
        });
        return result.get();
    }

    default <TAgent extends AbstractAgent> void forEach(Class<TAgent> type, Consumer<TAgent> consumer) {
        stream(type).forEach(consumer);
    }

    int size(Class<?> type);

    void clear(Class<?> type);

    void clearAll();
}
