package genesis;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import jade.Boot;
import jade.core.ProfileException;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.leap.Properties;
import jade.wrapper.ContainerController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import web.WebServer;
import web.WebServerFactory;

public class MainModule extends AbstractModule {
    private final static Logger logger = LoggerFactory.getLogger(MainModule.class);
    private final String[] args;

    public MainModule(String... args) {
        this.args = args;
    }

    @Provides
    @Singleton
    public ContainerController boot() {
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

        return new ProfileImpl();
    }


}
