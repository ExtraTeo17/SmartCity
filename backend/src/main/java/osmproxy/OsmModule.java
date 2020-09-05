package osmproxy;

import com.google.inject.Binder;
import com.google.inject.Singleton;
import genesis.AbstractModule;
import osmproxy.buses.BusApiManager;
import osmproxy.buses.BusLinesManager;
import osmproxy.buses.IBusApiManager;
import osmproxy.buses.IBusLinesManager;

public class OsmModule extends AbstractModule {
    @Override
    public void configure(Binder binder) {
        super.configure(binder);
        binder.bind(IBusApiManager.class).to(BusApiManager.class).in(Singleton.class);
        binder.bind(IBusLinesManager.class).to(BusLinesManager.class).in(Singleton.class);
    }
}
