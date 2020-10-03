package routing;

import com.google.inject.Binder;
import com.google.inject.Singleton;
import genesis.AbstractModule;
import routing.abstractions.IRouteGenerator;
import routing.abstractions.IRouteTransformer;
import routing.abstractions.IRoutingHelper;

public class RoutingModule extends AbstractModule {
    @Override
    public void configure(Binder binder) {
        super.configure(binder);
        binder.bind(IRoutingHelper.class).to(RoutingHelper.class).in(Singleton.class);
        binder.bind(IRouteGenerator.class).to(Router.class).in(Singleton.class);
        binder.bind(IRouteTransformer.class).to(Router.class).in(Singleton.class);
    }
}
