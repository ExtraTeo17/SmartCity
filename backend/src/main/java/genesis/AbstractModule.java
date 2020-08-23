package genesis;

import com.google.common.eventbus.EventBus;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

public abstract class AbstractModule implements Module {
    protected static final EventBus eventBus;

    static {
        eventBus = new EventBus();
    }

    @Override
    public void configure(Binder binder) {
        binder.bind(EventBus.class).toInstance(eventBus);
        binder.bindListener(Matchers.any(), new TypeListener() {
            public <I> void hear(TypeLiteral<I> typeLiteral, TypeEncounter<I> typeEncounter) {
                typeEncounter.register((InjectionListener<I>) eventBus::register);
            }
        });
    }
}
