package smartcity;

import com.google.inject.Binder;
import com.google.inject.Singleton;
import genesis.AbstractModule;

public class SmartCityModule extends AbstractModule {

    @Override
    public void configure(Binder binder) {
        super.configure(binder);

        binder.bind(MasterAgent.class).in(Singleton.class);
        binder.bind(IdGenerator.class).in(Singleton.class);
    }
}
