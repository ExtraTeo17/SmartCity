package smartcity.task.runnable;

import com.google.inject.Binder;
import com.google.inject.Singleton;
import genesis.AbstractModule;

public class RunnableModule extends AbstractModule {
    @Override
    public void configure(Binder binder) {
        super.configure(binder);
        binder.bind(IRunnableFactory.class).to(RunnableFactory.class).in(Singleton.class);
    }
}
