import com.google.inject.Guice;
import genesis.MainModule;
import genesis.WebModule;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartcity.MasterAgent;

public class SmartCity {
    private static final Logger logger = LoggerFactory.getLogger(SmartCity.class);

    public static void main(String[] args) {
        var injector = Guice.createInjector(
                new MainModule(args),
                new WebModule()
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
}
