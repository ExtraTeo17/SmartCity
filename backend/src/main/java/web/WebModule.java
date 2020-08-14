package web;

import com.google.inject.Binder;
import com.google.inject.PrivateModule;
import com.google.inject.Singleton;
import genesis.AbstractModule;
import web.abstractions.IWebConnector;
import web.abstractions.IWebService;
import web.serialization.SerializationModule;


public class WebModule extends AbstractModule {
    @Override
    public void configure(Binder binder) {
        super.configure(binder);

        binder.install(new SerializationModule());

        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                bind(IWebConnector.class).to(WebConnector.class).in(Singleton.class);
                bind(MessageHandler.class).in(Singleton.class);
                expose(MessageHandler.class);
            }
        });

        binder.bind(SocketServer.class).in(Singleton.class);
        binder.bind(IWebService.class).to(WebService.class).in(Singleton.class);
    }
}
