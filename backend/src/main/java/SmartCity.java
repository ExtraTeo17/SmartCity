import agents.AgentsModule;
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
import smartcity.lights.core.LightsModule;
import web.WebModule;

public class SmartCity {
    private static final Logger logger = LoggerFactory.getLogger(SmartCity.class);

    public static void main(String[] args) {
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
            agentController.activate();
            agentController.start();
        } catch (StaleProxyException e) {
            logger.error("Error accepting main agent", e);
            System.exit(-1);
        }
    }
}
