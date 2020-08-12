package agents;

import jade.core.Agent;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;


// I hate Java generics.
public interface IAgentsContainer<T extends Agent> extends IRegistrable<T> {
    boolean tryAdd(T agent);

    <TSpec extends T> Iterator<TSpec> iterator(Class<TSpec> type);

    boolean contains(T agent);

    <TSpec extends T> void removeIf(Class<TSpec> type, Predicate<TSpec> predicate);

    <TSpec extends T> void forEach(Class<TSpec> type, Consumer<TSpec> consumer);

    int size(Class<?> type);

    void clear(Class<?> type);
}
