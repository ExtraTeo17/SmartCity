package genesis;

import com.google.inject.Binder;
import com.google.inject.Singleton;
import gui.MapWindow;

@Deprecated
public class GuiModule extends AbstractModule {

    @Override
    public void configure(Binder binder) {
        super.configure(binder);

        binder.bind(MapWindow.class).in(Singleton.class);
    }
}
