import com.google.inject.Guice;
import genesis.MainModule;
import genesis.WebModule;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartcity.MasterAgent;
import utilities.ExtendedProperties;

import java.io.InputStream;
import java.net.URL;

public class SmartCity {
    private static final Logger logger = LoggerFactory.getLogger(SmartCity.class);

    public static void main(String[] args) {
        var prop = readProperties();
        int port = prop.getOrDefault("port", 8000);
        var injector = Guice.createInjector(
                new MainModule(args),
                new WebModule(port)
        );

        var controller = injector.getInstance(ContainerController.class);
        var mainAgent = injector.getInstance(MasterAgent.class);
        try {
            var name = MasterAgent.class.getName().replace("Agent", "");
            var agentController = controller.acceptNewAgent(name, mainAgent);
            agentController.activate();
            agentController.start();
        } catch (StaleProxyException e) {
            logger.error("Error accepting main agent", e);
            System.exit(-1);
        }
    }

    private static ExtendedProperties readProperties() {
        URL configFile = SmartCity.class.getClassLoader().getResource("config.properties");
        var prop = new ExtendedProperties();
        if (configFile != null) {
            try {
                InputStream configFileStream = configFile.openStream();
                prop.load(configFileStream);
                configFileStream.close();
            } catch (Exception e) {
                logger.warn("Unable to read properties", e);
            }
        }

        return prop;
    }

}
