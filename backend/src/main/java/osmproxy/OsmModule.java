package osmproxy;

import com.google.inject.Binder;
import com.google.inject.Singleton;
import genesis.AbstractModule;
import osmproxy.buses.BusLinesManager;
import osmproxy.buses.IBusLinesManager;

public class OsmModule extends AbstractModule {
    @Override
    public void configure(Binder binder) {
        super.configure(binder);
        binder.bind(IBusLinesManager.class).to(BusLinesManager.class).in(Singleton.class);
    }
}
