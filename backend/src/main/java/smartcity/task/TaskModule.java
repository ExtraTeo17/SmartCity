package smartcity.task;

import com.google.inject.Binder;
import com.google.inject.Singleton;
import genesis.AbstractModule;
import smartcity.task.runnable.RunnableModule;

public class TaskModule extends AbstractModule {
    @Override
    public void configure(Binder binder) {
        super.configure(binder);
        binder.install(new RunnableModule());
        binder.bind(ITaskManager.class).to(TaskManager.class).in(Singleton.class);
    }
}
