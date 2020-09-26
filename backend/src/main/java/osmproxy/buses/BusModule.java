package osmproxy.buses;

import com.google.inject.Binder;
import com.google.inject.PrivateModule;
import com.google.inject.Singleton;
import genesis.AbstractModule;
import osmproxy.buses.abstractions.*;
import osmproxy.buses.serialization.WarszawskieApiSerializer;

public class BusModule extends AbstractModule {
    @Override
    public void configure(Binder binder) {
        super.configure(binder);
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                bind(IApiSerializer.class).to(WarszawskieApiSerializer.class).in(Singleton.class);
                bind(IBusApiManager.class).to(BusApiManager.class).in(Singleton.class);
                bind(IDataMerger.class).to(DataMerger.class).in(Singleton.class);
                bind(IBusDataParser.class).to(BusDataParser.class).in(Singleton.class);
                bind(IBusLinesManager.class).to(BusLinesManager.class).in(Singleton.class);
                expose(IBusLinesManager.class);
            }
        });
    }
}
