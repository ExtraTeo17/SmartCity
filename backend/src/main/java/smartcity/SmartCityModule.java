package smartcity;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import genesis.AbstractModule;
import routing.core.IZone;
import smartcity.config.ConfigContainer;
import smartcity.task.ITaskManager;
import smartcity.task.TaskManager;
import smartcity.task.TaskModule;

public class SmartCityModule extends AbstractModule {

    @Override
    public void configure(Binder binder) {
        super.configure(binder);
        binder.install(new TaskModule());
        binder.bind(MasterAgent.class).in(Singleton.class);
        binder.bind(ConfigContainer.class).in(Singleton.class);
        binder.bind(ITimeManager.class).to(TimeManager.class).asEagerSingleton();
    }

    @Provides
    @Singleton
    @Inject
    IZone getZone(ConfigContainer configContainer) {
        return configContainer.getZone();
    }
}
