package smartcity.task.functional;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import genesis.AbstractModule;
import smartcity.lights.core.LightSwitcher;
import smartcity.task.data.ISwitchLightsContext;

import java.util.function.Function;

public class FunctionalModule extends AbstractModule {
    @Override
    public void configure(Binder binder) {
        super.configure(binder);
        binder.install(new FactoryModuleBuilder()
                .implement(new TypeLiteral<Function<ISwitchLightsContext, Integer>>() {}, LightSwitcher.class)
                .build(IFunctionalTaskFactory.class));
    }
}
