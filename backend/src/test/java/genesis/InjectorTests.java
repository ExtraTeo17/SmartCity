package genesis;

import agents.AgentsModule;
import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Guice;
import com.google.inject.Injector;
import events.ClearSimulationEvent;
import events.LightManagersReadyEvent;
import events.SwitchLightsStartEvent;
import events.web.PrepareSimulationEvent;
import events.web.SimulationPreparedEvent;
import events.web.SimulationStartedEvent;
import events.web.StartSimulationEvent;
import events.web.bike.BikeAgentCreatedEvent;
import events.web.bike.BikeAgentDeadEvent;
import events.web.bike.BikeAgentUpdatedEvent;
import events.web.bus.BusAgentDeadEvent;
import events.web.bus.BusAgentFillStateUpdatedEvent;
import events.web.bus.BusAgentUpdatedEvent;
import events.web.car.CarAgentCreatedEvent;
import events.web.car.CarAgentDeadEvent;
import events.web.car.CarAgentRouteChangedEvent;
import events.web.car.CarAgentUpdatedEvent;
import events.web.pedestrian.*;
import events.web.roadblocks.TrafficJamFinishedEvent;
import events.web.roadblocks.TrafficJamStartedEvent;
import events.web.roadblocks.TroublePointCreatedEvent;
import org.junit.jupiter.api.Test;
import osmproxy.OsmModule;
import osmproxy.buses.BusModule;
import osmproxy.routes.OsmRoutesModule;
import routing.RoutingModule;
import smartcity.SmartCityModule;
import smartcity.config.ConfigMutator;
import smartcity.lights.core.LightsModule;
import testutils.ReflectionHelper;
import web.WebModule;

import java.time.LocalDateTime;
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
                        new WebModule(4002),
                        new BusModule(),
                        new OsmModule(),
                        new OsmRoutesModule(),
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
        // Some events will throw but we are testing handle-invoke

        //  Simulation-related
        eventBus.post(new PrepareSimulationEvent(null, false));
        eventBus.post(new LightManagersReadyEvent(null));
        eventBus.post(new SimulationPreparedEvent(new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
        eventBus.post("Test"); // Dead event
        eventBus.post(new StartSimulationEvent(false, 0, 0,
                false, false, 0,
                0, 5000, 0,
                false, 1, false,
                true, true, true,
                LocalDateTime.now(), 11, false,
                30, false, 60,
                false, 1, 1,
                false, true
        ));
        eventBus.post(new SimulationStartedEvent());
        eventBus.post(new ClearSimulationEvent());

        // other
        eventBus.post(new TroublePointCreatedEvent(1, null));
        eventBus.post(new SwitchLightsStartEvent(1, null));
        eventBus.post(new TrafficJamStartedEvent(1));
        eventBus.post(new TrafficJamFinishedEvent(1));

        // vehicle
        eventBus.post(new CarAgentCreatedEvent(1, null, null, false));
        eventBus.post(new CarAgentUpdatedEvent(1, null));
        eventBus.post(new CarAgentRouteChangedEvent(1, new ArrayList<>(), null, new ArrayList<>()));
        eventBus.post(new CarAgentDeadEvent(1, 0, null));

        // bike
        eventBus.post(new BikeAgentCreatedEvent(1, null, null, false));
        eventBus.post(new BikeAgentUpdatedEvent(1, null));
        eventBus.post(new BikeAgentDeadEvent(1, 0, null));

        eventBus.post("Test"); // Dead event

        // bus
        eventBus.post(new BusAgentDeadEvent(1));
        eventBus.post(new BusAgentFillStateUpdatedEvent(0, null));
        eventBus.post(new BusAgentUpdatedEvent(1, null));
        eventBus.post(new BusAgentDeadEvent(1));

        // pedestrian
        eventBus.post(new PedestrianAgentCreatedEvent(1, null, new ArrayList<>(), new ArrayList<>(), false));
        eventBus.post(new PedestrianAgentUpdatedEvent(1, null));
        eventBus.post(new PedestrianAgentEnteredBusEvent(1));
        eventBus.post(new PedestrianAgentLeftBusEvent(1, null, false));
        eventBus.post(new PedestrianAgentDeadEvent(1, 0, null));

        int expectedDeadEvents = 3;
        assertEquals(expectedDeadEvents, counter.get(), "All events should be handled somewhere");
    }
}
