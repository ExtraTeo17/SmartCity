import agents.AgentsModule;
import com.google.inject.Guice;
import genesis.GuiModule;
import genesis.MainModule;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import osmproxy.OsmModule;
import smartcity.MasterAgent;
import smartcity.SmartCityModule;
import web.WebModule;

public class SmartCity {
    private static final Logger logger = LoggerFactory.getLogger(SmartCity.class);

    public static void main(String[] args) {
        var injector = Guice.createInjector(
                new MainModule(args),
                new AgentsModule(),
                new GuiModule(),
                new WebModule(),
                new SmartCityModule(),
                new OsmModule()
        );

        var controller = injector.getInstance(ContainerController.class);
        var mainAgent = injector.getInstance(MasterAgent.class);
        try {
            var agentController = controller.acceptNewAgent(MasterAgent.name, mainAgent);
            agentController.activate();
            agentController.start();
        } catch (StaleProxyException e) {
            logger.error("Error accepting main agent", e);
            System.exit(-1);
        }
    }
}
