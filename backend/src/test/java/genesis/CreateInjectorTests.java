package genesis;

import agents.AgentsModule;
import com.google.inject.Guice;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import osmproxy.OsmModule;
import osmproxy.buses.BusModule;
import routing.RoutingModule;
import smartcity.SmartCityModule;
import web.WebModule;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestMethodOrder(MethodOrderer.Alphanumeric.class)
class CreateInjectorTests {
    @Test
    void getInstance_fromAllModules() {
        // Arrange
        var injector =
                Guice.createInjector(
                        new MainModule(4001),
                        new SharedModule(),
                        new AgentsModule(),
                        new GuiModule(),
                        new WebModule(4002),
                        new BusModule(),
                        new OsmModule(),
                        new RoutingModule(),
                        new SmartCityModule()
                );

        // Act & Assert
        for (var bindingEntry : injector.getAllBindings().entrySet()) {
            var key = bindingEntry.getKey();
            var instance = injector.getInstance(key);
            assertNotNull(instance, "Instance for key: '" + key.toString() + "' should not be null.");
        }
    }
}
