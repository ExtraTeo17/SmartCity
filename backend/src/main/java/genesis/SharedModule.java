package genesis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class SharedModule extends AbstractModule {
    @Provides
    @Singleton
    ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }
}
