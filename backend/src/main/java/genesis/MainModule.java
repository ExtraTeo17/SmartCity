package genesis;

import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import jade.Boot;
import jade.core.ProfileException;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.leap.Properties;
import jade.wrapper.ContainerController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartcity.config.ConfigProperties;
import utilities.ExtendedProperties;

import java.io.InputStream;
import java.net.URL;


public class MainModule extends AbstractModule {
    private static final Logger logger = LoggerFactory.getLogger(MainModule.class);

    private final int port;
    private final String[] args;

    public MainModule(String... args) {
        this(ConfigProperties.JADE_PORT, args);
    }

    MainModule(Integer port, String... args) {
        this.port = port;
        this.args = args;
    }

    @Override
    public void configure(Binder binder) {
        super.configure(binder);
        binder.bind(Integer.class)
                .annotatedWith(Names.named("JADE_PORT"))
                .toInstance(port);
    }

    @Provides
    @Singleton
    private ContainerController boot() {
        ProfileImpl profile = createProfile();
        Runtime.instance().setCloseVM(true);
        return Runtime.instance().createMainContainer(profile);
    }

    private ProfileImpl createProfile() {
        if (args.length > 0) {
            if (args[0].startsWith("-")) {
                Properties properties = Boot.parseCmdLineArgs(args);
                if (properties == null) {
                    logger.warn("Arguments were not recognized: " + String.join(" ", args));
                }
                else {
                    return new ProfileImpl(properties);
                }
            }
            else {
                try {
                    return new ProfileImpl(args[0]);
                } catch (ProfileException e) {
                    logger.warn("Argument not recognized" + args[0]);
                }
            }
        }

        return new ProfileImpl("localhost", port, "1");
    }

    @Provides
    @Singleton
    private ExtendedProperties readProperties() {
        URL configFile = MainModule.class.getClassLoader().getResource("config.properties");
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
