package smartcity;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import genesis.AbstractModule;
import routing.core.IZone;
import smartcity.config.ConfigContainer;
import smartcity.config.abstractions.ITroublePointsConfigContainer;
import smartcity.task.TaskModule;

public class SmartCityModule extends AbstractModule {

    @Override
    public void configure(Binder binder) {
        super.configure(binder);
        binder.bind(ITimeProvider.class).to(TimeProvider.class).asEagerSingleton();
        binder.bind(ConfigContainer.class).asEagerSingleton();
        binder.bind(ITroublePointsConfigContainer.class).to(ConfigContainer.class);

        binder.install(new TaskModule());
        binder.bind(SmartCityAgent.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    @Inject
    IZone getZone(ConfigContainer configContainer) {
        return configContainer.getZone();
    }
}
