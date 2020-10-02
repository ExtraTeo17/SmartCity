package smartcity.task;

import com.google.inject.Binder;
import com.google.inject.Singleton;
import genesis.AbstractModule;
import smartcity.task.abstractions.ITaskManager;
import smartcity.task.abstractions.ITaskProvider;
import smartcity.task.functional.FunctionalModule;
import smartcity.task.runnable.RunnableModule;

public class TaskModule extends AbstractModule {
    @Override
    public void configure(Binder binder) {
        super.configure(binder);
        binder.install(new RunnableModule());
        binder.install(new FunctionalModule());
        binder.bind(ITaskProvider.class).to(TaskProvider.class).in(Singleton.class);
        binder.bind(ITaskManager.class).to(TaskManager.class).asEagerSingleton();
    }
}
