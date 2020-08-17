package agents.abstractions;

import agents.AbstractAgent;
import jade.core.Agent;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;


// I hate Java generics.
public interface IAgentsContainer extends IRegistrable {
    boolean tryAdd(AbstractAgent agent);

    <TAgent extends Agent> Iterator<TAgent> iterator(Class<TAgent> type);

    boolean contains(Agent agent);

    <TAgent extends Agent> void removeIf(Class<TAgent> type, Predicate<TAgent> predicate);

    <TAgent extends Agent> void forEach(Class<TAgent> type, Consumer<TAgent> consumer);

    int size(Class<?> type);

    void clear(Class<?> type);
}
