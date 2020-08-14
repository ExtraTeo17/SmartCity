package smartcity;

import com.google.inject.Binder;
import com.google.inject.Singleton;
import genesis.AbstractModule;
import smartcity.task.TaskManager;

public class SmartCityModule extends AbstractModule {

    @Override
    public void configure(Binder binder) {
        super.configure(binder);

        binder.bind(MasterAgent.class).in(Singleton.class);
        binder.bind(TaskManager.class).in(Singleton.class);
    }
}
