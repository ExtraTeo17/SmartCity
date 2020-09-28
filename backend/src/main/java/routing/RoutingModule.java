package routing;

import com.google.inject.Binder;
import com.google.inject.Singleton;
import genesis.AbstractModule;
import utilities.Siblings;

public class RoutingModule  extends AbstractModule {
    @Override
    public void configure(Binder binder) {
        super.configure(binder);
        binder.bind(IRouteGenerator.class).to(Router.class).in(Singleton.class);
        binder.bind(IRouteTransformer.class).to(Router.class).in(Singleton.class);
    }
}
