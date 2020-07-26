package genesis;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import web.WebServer;
import web.WebServerFactory;

public class WebModule extends AbstractModule {
    private final int port;

    public WebModule(int port) {
        this.port = port;
    }

    @Provides
    @Singleton
    public WebServer createWebServer() {
        return WebServerFactory.create(port);
    }
}
