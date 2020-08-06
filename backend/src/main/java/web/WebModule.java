package web;

import com.google.inject.Binder;
import com.google.inject.Singleton;
import genesis.AbstractModule;
import web.serialization.IMessageObjectMapper;
import web.serialization.MessageObjectMapper;


public class WebModule extends AbstractModule {
    public WebModule() {
    }

    @Override
    public void configure(Binder binder) {
        super.configure(binder);

        binder.bind(SocketServer.class).in(Singleton.class);
        binder.bind(IWebConnector.class).to(WebConnector.class).in(Singleton.class);
        binder.bind(IMessageObjectMapper.class).to(MessageObjectMapper.class).in(Singleton.class);
        binder.bind(IWebService.class).to(WebService.class).in(Singleton.class);
    }
}
