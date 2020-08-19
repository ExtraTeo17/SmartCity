package smartcity.task.runnable;

import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import genesis.AbstractModule;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class RunnableModule extends AbstractModule {
    @Override
    public void configure(Binder binder) {
        super.configure(binder);
        binder.install(new FactoryModuleBuilder()
                .implement(IFixedExecutionRunnable.class, FixedExecutionRunnable.class)
                .build(IRunnableFactory.class));
    }

    @Provides
    @Singleton
    ScheduledExecutorService getExecutorService() {
        return Executors.newSingleThreadScheduledExecutor();
    }
}
