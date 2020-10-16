import agents.AgentsModule;
import agents.TroubleManagerAgent;
import com.google.inject.Guice;
import genesis.GuiModule;
import genesis.MainModule;
import genesis.SharedModule;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import osmproxy.OsmModule;
import osmproxy.buses.BusModule;
import routing.RoutingModule;
import smartcity.SmartCityAgent;
import smartcity.SmartCityModule;
import smartcity.config.ConfigProperties;
import smartcity.lights.core.LightsModule;
import web.WebModule;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Properties;

public class SmartCity {
    private static final Logger logger = LoggerFactory.getLogger(SmartCity.class);

    public static void main(String[] args) {
        setupProperties();
        var injector = Guice.createInjector(
                new MainModule(args),
                new SharedModule(),
                new LightsModule(),
                new AgentsModule(),
                new GuiModule(),
                new WebModule(),
                new BusModule(),
                new OsmModule(),
                new RoutingModule(),
                new SmartCityModule()
        );

        var controller = injector.getInstance(ContainerController.class);
        var mainAgent = injector.getInstance(SmartCityAgent.class);
        try {
            var agentController = controller.acceptNewAgent(SmartCityAgent.name, mainAgent);
            var mainAgent2 = injector.getInstance(TroubleManagerAgent.class);
            var agentController2 = controller.acceptNewAgent(TroubleManagerAgent.name, mainAgent2);
            agentController.activate();
            agentController.start();
            agentController2.activate();
            agentController2.start();
        } catch (StaleProxyException e) {
            logger.error("Error accepting main agent", e);
            System.exit(-1);
        }
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
