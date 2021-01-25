import agents.AgentsModule;
import com.google.inject.Guice;
import genesis.MainModule;
import genesis.SharedModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import osmproxy.OsmModule;
import osmproxy.buses.BusModule;
import osmproxy.routes.OsmRoutesModule;
import routing.RoutingModule;
import smartcity.SmartCityModule;
import smartcity.config.ConfigProperties;
import smartcity.lights.core.LightsModule;
import web.WebModule;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Optional;
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
                new OsmRoutesModule(),
                new RoutingModule(),
                new SmartCityModule()
        );
    }

    private static void setupProperties() {
        var uriLocation = getURILocation();
        if (uriLocation.isEmpty()) {
            return;
        }

        var uriLocationPath = Paths.get(uriLocation.get()).toString();
        var configFile = Paths.get(uriLocationPath, "config.properties").toFile();
        if (!configFile.exists()) {
            logger.warn("Did not find config.properties: " + configFile.getAbsolutePath());
            return;
        }

        var props = new Properties();
        try (InputStream configFileStream = new FileInputStream(configFile)) {
            props.load(configFileStream);
            props.forEach((key, value) -> setStatic((String) key, value));
        } catch (Exception e) {
            logger.warn("Unable to read properties", e);
        }
    }

    private static Optional<URI> getURILocation() {
        URL location = SmartCity.class.getProtectionDomain().getCodeSource().getLocation();
        if (location == null) {
            logger.warn("Unable to get code source location to read properties");
            return Optional.empty();
        }

        if (!location.getPath().endsWith("/")) {
            try {
                var locationString = location.toString();
                var lastIndexOfSlash = locationString.lastIndexOf("/");
                location = new URL(locationString.substring(0, lastIndexOfSlash + 1));
            } catch (MalformedURLException e) {
                logger.warn("Unable to get path for location: " + location.toString(), e);
                return Optional.empty();
            }
        }

        URI uriLocation;
        try {
            uriLocation = location.toURI();
        } catch (URISyntaxException e) {
            logger.warn("Unable to convert to URI", e);
            return Optional.empty();
        }

        return Optional.of(uriLocation);
    }

    private static void setStatic(String fieldName, Object newValue) {
        try {
            var field = ConfigProperties.class.getField(fieldName);
            var parsedValue = getParsedValue(field, newValue);
            field.set(null, parsedValue);
            logger.debug("Successfully set " + fieldName + " to " + newValue.toString());
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
        else if (type == String[].class) {
            return stringValue.split(",");
        }

        return newValue;
    }
}
