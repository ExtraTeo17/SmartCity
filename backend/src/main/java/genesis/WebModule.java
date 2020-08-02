package genesis;

import com.google.inject.Binder;
import com.google.inject.Singleton;
import web.IWebManager;
import web.WebManager;
import web.WebServer;
import web.serialization.IMessageObjectMapper;
import web.serialization.MessageObjectMapper;


public class WebModule extends AbstractModule {
    public WebModule() {
    }

    @Override
    public void configure(Binder binder) {
        super.configure(binder);
        binder.bind(WebServer.class).in(Singleton.class);
        binder.bind(IWebManager.class).to(WebManager.class).in(Singleton.class);
        binder.bind(IMessageObjectMapper.class).to(MessageObjectMapper.class).in(Singleton.class);
    }
}
