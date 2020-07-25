package genesis;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.ContainerController;
import web.WebServer;
import web.WebServerFactory;

public class MainModule implements Module {
    @Override
    public void configure(Binder binder) {
    }

    @Provides
    @Singleton
    public static ContainerController boot() {
        ProfileImpl p = new ProfileImpl();
        Runtime.instance().setCloseVM(true);
        return Runtime.instance().createMainContainer(p);
    }

    @Provides
    public static WebServer createWebServer() {
        return WebServerFactory.create(9000);
    }
}
