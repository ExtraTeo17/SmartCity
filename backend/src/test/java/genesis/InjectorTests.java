package genesis;

import agents.AgentsModule;
import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Guice;
import com.google.inject.Injector;
import events.LightManagersReadyEvent;
import events.SwitchLightsStartEvent;
import events.web.*;
import org.junit.jupiter.api.Test;
import osmproxy.OsmModule;
import osmproxy.buses.BusModule;
import routing.RoutingModule;
import smartcity.SmartCityModule;
import smartcity.config.ConfigMutator;
import smartcity.lights.core.LightsModule;
import testutils.ReflectionHelper;
import web.WebModule;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SuppressWarnings("OverlyCoupledClass")
class InjectorTests {
    @Test
    void getInstance_fromAllModules() {
        // Arrange
        ReflectionHelper.setStatic("counter", ConfigMutator.class, 0);
        var injector =
                Guice.createInjector(
                        new MainModule(4001),
                        new SharedModule(),
                        new LightsModule(),
                        new AgentsModule(),
                        new GuiModule(),
                        new WebModule(4002),
                        new BusModule(),
                        new OsmModule(),
                        new RoutingModule(),
                        new SmartCityModule()
                );

        // Act & Assert
        assertInstancesNotNull(injector);
        assertEventsHandled(injector);
    }

    private void assertInstancesNotNull(Injector injector) {
        for (var bindingEntry : injector.getAllBindings().entrySet()) {
            var key = bindingEntry.getKey();
            var instance = injector.getInstance(key);
            assertNotNull(instance, "Instance for key: '" + key.toString() + "' should not be null.");
        }
    }

    private void assertEventsHandled(Injector injector) {
        AtomicInteger counter = new AtomicInteger();
        var deadEventListener = new Object() {
            @Subscribe
            void handle(DeadEvent event) {
                counter.getAndIncrement();
            }
        };
        var eventBus = injector.getInstance(EventBus.class);
        eventBus.register(deadEventListener);

        eventBus.post("Test"); // Dead event
        // Will throw but we are testing handle-invoke
        eventBus.post(new PrepareSimulationEvent(null));
        eventBus.post(new LightManagersReadyEvent(null));
        eventBus.post(new SimulationPreparedEvent(new ArrayList<>()));
        eventBus.post("Test"); // Dead event
        eventBus.post(new StartSimulationEvent(0, 0));
        eventBus.post(new SimulationStartedEvent());
        eventBus.post(new VehicleAgentCreatedEvent(1, null, false));
        eventBus.post(new VehicleAgentUpdatedEvent(1, null));
        eventBus.post(new SwitchLightsStartEvent(1, null));
        eventBus.post("Test"); // Dead event


        int expectedDeadEvents = 3;
        assertEquals(expectedDeadEvents, counter.get(), "All events should be handled somewhere");
    }
}
