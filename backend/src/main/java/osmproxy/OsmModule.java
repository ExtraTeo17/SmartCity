package osmproxy;

import com.google.inject.Binder;
import com.google.inject.Singleton;
import genesis.AbstractModule;
import osmproxy.buses.BusApiManager;
import osmproxy.buses.BusDataMerger;
import osmproxy.buses.BusDataParser;
import osmproxy.buses.BusLinesManager;
import osmproxy.buses.abstractions.IBusApiManager;
import osmproxy.buses.abstractions.IBusDataMerger;
import osmproxy.buses.abstractions.IBusDataParser;
import osmproxy.buses.abstractions.IBusLinesManager;

public class OsmModule extends AbstractModule {
    @Override
    public void configure(Binder binder) {
        super.configure(binder);
        binder.bind(IBusApiManager.class).to(BusApiManager.class).in(Singleton.class);
        binder.bind(IBusDataMerger.class).to(BusDataMerger.class).in(Singleton.class);
        binder.bind(IBusDataParser.class).to(BusDataParser.class).in(Singleton.class);
        binder.bind(IBusLinesManager.class).to(BusLinesManager.class).in(Singleton.class);
    }
}
