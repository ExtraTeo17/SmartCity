import agents.AgentsModule;
import com.google.inject.Guice;
import genesis.MainModule;
import genesis.SharedModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import osmproxy.OsmModule;
import osmproxy.buses.BusModule;
import routing.RoutingModule;
import smartcity.SmartCityModule;
import smartcity.config.ConfigProperties;
import smartcity.lights.core.LightsModule;
import web.WebModule;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Properties;

/**
 * The main module of the application which creates all the other necessary
 * modules.
 */
public class SmartCity {
    private static final Logger logger = LoggerFactory.getLogger(SmartCity.class);

    public static void main(String[] args) {
        setupProperties();
        Guice.createInjector(
                new MainModule(args),
                new SharedModule(),
                new LightsModule(),
                new AgentsModule(),
                new WebModule(),
                new BusModule(),
                new OsmModule(),
                new RoutingModule(),
                new SmartCityModule()
        );
    }

    private static void setupProperties() {
        URL configFile = SmartCity.class.getClassLoader().getResource("config.properties");
        var props = new Properties();
        if (configFile != null) {
            try {
                InputStream configFileStream = configFile.openStream();
                props.load(configFileStream);
                configFileStream.close();
            } catch (Exception e) {
                logger.warn("Unable to read properties", e);
            }
        }

        props.forEach((key, value) -> setFinalStatic((String) key, value));
    }

    private static void setFinalStatic(String fieldName, Object newValue) {
        try {
            var field = ConfigProperties.class.getField(fieldName);
            var parsedValue = getParsedValue(field, newValue);
            field.set(null, parsedValue);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            logger.warn("Error trying to setup config for: " + fieldName, e);
        }
    }

    private static Object getParsedValue(Field field, Object newValue) {
        var type = field.getType();
        var stringValue = (String) newValue;
        if (type == boolean.class) {
            return Boolean.parseBoolean(stringValue);
        }
        else if (type == int.class) {
            return Integer.parseInt(stringValue);
        }

        return newValue;
    }
}
