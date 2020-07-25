import com.google.inject.Guice;
import genesis.MainModule;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartcity.MainContainerAgent;

public class SmartCity {
    private static Logger logger = LoggerFactory.getLogger(SmartCity.class);

    public static void main(String[] args) {
        var injector = Guice.createInjector(new MainModule());

        // TODO: Inject configuration - port/main name, etc.
        var controller = injector.getInstance(ContainerController.class);
        var mainAgent = injector.getInstance(MainContainerAgent.class);
        try {
            var agentController = controller.acceptNewAgent("Master", mainAgent);
            agentController.activate();
            agentController.start();
        } catch (StaleProxyException e) {
            logger.error("Error accepting main agent", e);
            System.exit(-1);
        }
    }
}
