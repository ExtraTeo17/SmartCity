package behaviourfactories;

import agents.abstractions.AbstractAgent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;

public interface IBehaviourFactory<T extends AbstractAgent> {
    CyclicBehaviour createCyclicBehaviour(T agent);

    TickerBehaviour createTickerBehaviour(T agent);
}
