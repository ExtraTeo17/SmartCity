package genesis;

import com.google.inject.Binder;
import org.java_websocket.server.WebSocketServer;
import web.WebServer;


public class WebModule extends AbstractModule {
    public WebModule() {
    }

    @Override
    public void configure(Binder binder) {
        super.configure(binder);
        binder.bind(WebSocketServer.class).to(WebServer.class);
    }
}
