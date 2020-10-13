package osmproxy;

import com.google.inject.Binder;
import com.google.inject.Singleton;
import genesis.AbstractModule;
import osmproxy.abstractions.ILightAccessManager;
import osmproxy.abstractions.IMapAccessManager;

public class OsmModule extends AbstractModule {
    @Override
    public void configure(Binder binder) {
        binder.bind(IMapAccessManager.class).to(MapAccessManager.class).in(Singleton.class);
        binder.bind(ILightAccessManager.class).to(LightAccessManager.class).in(Singleton.class);
    }
}
