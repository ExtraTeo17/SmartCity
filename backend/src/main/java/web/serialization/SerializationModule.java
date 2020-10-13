package web.serialization;

import com.google.inject.Binder;
import com.google.inject.Singleton;
import genesis.AbstractModule;
import web.abstractions.IMessageObjectMapper;

public class SerializationModule extends AbstractModule {
    @Override
    public void configure(Binder binder) {
        super.configure(binder);
        binder.bind(IMessageObjectMapper.class).to(MessageObjectMapper.class).in(Singleton.class);
    }
}
