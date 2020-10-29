package agents;

import agents.abstractions.AbstractAgent;
import com.google.common.eventbus.EventBus;

import smartcity.ITimeProvider;
import smartcity.recreationalplaces.OSMCafe;


public class CafeAgent extends AbstractAgent {
    public static final String name = CafeAgent.class.getSimpleName().replace("Agent", "");

    private final OSMCafe cafe;

    CafeAgent(int id,
                 OSMCafe cafe,
                 ITimeProvider timeProvider,
                 EventBus eventBus) {
        super(id, name, timeProvider, eventBus);
        this.cafe = cafe;

    }
    OSMCafe getCafe(){return cafe;}
}
