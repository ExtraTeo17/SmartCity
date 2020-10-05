package web;

import com.google.inject.*;
import com.google.inject.name.Names;
import genesis.AbstractModule;
import smartcity.config.ConfigProperties;
import web.abstractions.IWebConnector;
import web.abstractions.IWebService;
import web.serialization.SerializationModule;


public class WebModule extends AbstractModule {
    private final Integer port;

    public WebModule() {
        this(ConfigProperties.WEB_PORT);
    }

    public WebModule(int port) {
        this.port = port;
    }

    @Override
    public void configure(Binder binder) {
        super.configure(binder);
        binder.install(new SerializationModule());
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                bind(Integer.class)
                        .annotatedWith(Names.named("WEB_PORT"))
                        .toInstance(port);
                bind(SocketServer.class).in(Singleton.class);

                bind(IWebConnector.class).to(WebConnector.class).in(Singleton.class);
                bind(MessageHandler.class).in(Singleton.class);
                bind(SocketServer.class).in(Singleton.class);
                bind(Communicator.class).asEagerSingleton();
            }

            @Provides
            @Singleton
            @Inject
            IWebService getWebService(WebConnector connector) {
                var service = new WebService(connector);
                service.start();
                return service;
            }
        });
    }
}
