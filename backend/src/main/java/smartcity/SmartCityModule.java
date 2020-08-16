package smartcity;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import genesis.AbstractModule;
import routing.IZone;
import smartcity.task.TaskManager;

public class SmartCityModule extends AbstractModule {

    @Override
    public void configure(Binder binder) {
        super.configure(binder);

        binder.bind(MasterAgent.class).in(Singleton.class);
        binder.bind(TaskManager.class).in(Singleton.class);
        binder.bind(ConfigContainer.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    @Inject
    IZone getZone(ConfigContainer configContainer) {
        return configContainer.getZone();
    }
}
