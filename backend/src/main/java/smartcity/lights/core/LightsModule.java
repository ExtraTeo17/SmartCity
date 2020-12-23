package smartcity.lights.core;

import com.google.inject.Binder;
import com.google.inject.Singleton;
import genesis.AbstractModule;
import smartcity.lights.abstractions.ICrossroadFactory;
import smartcity.lights.abstractions.ICrossroadParser;

public class LightsModule extends AbstractModule {
    @Override
    public void configure(Binder binder) {
        super.configure(binder);
        binder.bind(ICrossroadParser.class).to(CrossroadParser.class).in(Singleton.class);
        binder.bind(ICrossroadFactory.class).to(CrossroadFactory.class).in(Singleton.class);
    }
}
