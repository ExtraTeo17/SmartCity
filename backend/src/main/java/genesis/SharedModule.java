package genesis;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import java.util.Random;

public class SharedModule extends AbstractModule {

    @Provides
    @Singleton
    Random getRandom() {
        return new Random();
    }

    @Provides
    @Singleton
    ObjectMapper getObjectMapper() {
        var mapper = new ObjectMapper();
        var timeModule = new JavaTimeModule();
        mapper.registerModule(timeModule);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return mapper;
    }

    @Provides
    @Singleton
    @Inject
    ObjectWriter getObjectMapper(ObjectMapper mapper) {
        return mapper.writer();
    }
}
