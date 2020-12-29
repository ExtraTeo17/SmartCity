package osmproxy;

import com.google.inject.Binder;
import com.google.inject.PrivateModule;
import com.google.inject.Singleton;
import genesis.AbstractModule;
import osmproxy.abstractions.ICacheWrapper;
import osmproxy.abstractions.ILightAccessManager;
import osmproxy.abstractions.IMapAccessManager;
import osmproxy.abstractions.IOverpassApiManager;
import osmproxy.utilities.CacheWrapper;
import osmproxy.utilities.OverpassApiManager;

public class OsmModule extends AbstractModule {
    @Override
    public void configure(Binder binder) {
        binder.bind(ICacheWrapper.class).to(CacheWrapper.class).in(Singleton.class);
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                bind(IOverpassApiManager.class).to(OverpassApiManager.class).in(Singleton.class);
                expose(IOverpassApiManager.class);
            }
        });
        binder.bind(IMapAccessManager.class).to(MapAccessManager.class).in(Singleton.class);
        binder.bind(ILightAccessManager.class).to(LightAccessManager.class).in(Singleton.class);
    }
}
