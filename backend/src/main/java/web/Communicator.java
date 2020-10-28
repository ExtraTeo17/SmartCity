package web;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import events.web.SimulationPreparedEvent;
import events.web.SimulationStartedEvent;
import events.web.bus.BusAgentDeadEvent;
import events.web.bus.BusAgentFillStateUpdatedEvent;
import events.web.bus.BusAgentStartedEvent;
import events.web.bus.BusAgentUpdatedEvent;
import events.web.pedestrian.*;
import events.web.roadblocks.TroublePointCreatedEvent;
import events.web.roadblocks.TroublePointVanishedEvent;
import events.web.vehicle.VehicleAgentCreatedEvent;
import events.web.vehicle.VehicleAgentDeadEvent;
import events.web.vehicle.VehicleAgentRouteChangedEvent;
import events.web.vehicle.VehicleAgentUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartcity.ITimeProvider;
import smartcity.TimeProvider;
import web.abstractions.IWebService;

class Communicator {
    private static final Logger logger = LoggerFactory.getLogger(Communicator.class);

    private final IWebService webService;
    private final ITimeProvider timeProvider;

    @Inject
    public Communicator(IWebService webService,
                        ITimeProvider timeProvider) {
        this.webService = webService;
        this.timeProvider = timeProvider;
    }

    @Subscribe
    public void handle(SimulationPreparedEvent e) {
        onHandle(e, "Lights: " + e.lights.size() +
                ", stations: " + e.stations.size() +
                ", buses: " + e.buses.size());
        webService.prepareSimulation(e.lights, e.stations, e.buses);
    }

    @Subscribe
    public void handle(SimulationStartedEvent e) {
        webService.startSimulation(TimeProvider.TIME_SCALE);
    }

    @Subscribe
    public void handle(VehicleAgentCreatedEvent e) {
        onHandle(e);
        webService.createCar(e.agentId, e.agentPosition, e.route, e.isTestCar);
    }

    @Subscribe
    public void handle(VehicleAgentUpdatedEvent e) {
        webService.updateCar(e.id, e.position);
    }

    @Subscribe
    public void handle(SimulationPreparedEvent.SwitchLightsEvent e) {
        webService.updateLights(e.osmLightId);
    }

    @Subscribe
    public void handle(VehicleAgentDeadEvent e) {
        onHandle(e);
        webService.killCar(e.id, e.travelDistance, e.travelTime);
    }

    @Subscribe
    public void handle(VehicleAgentRouteChangedEvent e) {
        webService.changeRoute(e.agentId, e.routeStart, e.changePosition, e.routeEnd);
    }

    @Subscribe
    public void handle(TroublePointCreatedEvent e) {
        webService.createTroublePoint(e.id, e.position);
    }

    @Subscribe
    public void handle(TroublePointVanishedEvent e) {
        webService.hideTroublePoint(e.id);
    }

    @Subscribe
    public void handle(BusAgentStartedEvent e) {
    }

    @Subscribe
    public void handle(BusAgentUpdatedEvent e) {
        webService.updateBus(e.id, e.position);
    }

    @Subscribe
    public void handle(BusAgentFillStateUpdatedEvent e) {
        webService.updateBusFillState(e.id, e.fillState);
    }

    @Subscribe
    public void handle(BusAgentDeadEvent e) {
        onHandle(e);
        webService.killBus(e.id);
    }

    @Subscribe
    public void handle(PedestrianAgentCreatedEvent e) {
        webService.createPedestrian(e.id, e.position, e.routeToStation, e.routeFromStation, e.isTestPedestrian);
    }

    @Subscribe
    public void handle(PedestrianAgentUpdatedEvent e) {
        webService.updatePedestrian(e.id, e.position);
    }

    @Subscribe
    public void handle(PedestrianAgentEnteredBusEvent e) {
        webService.pushPedestrianIntoBus(e.id);
    }

    @Subscribe
    public void handle(PedestrianAgentLeftBusEvent e) {
        webService.pullPedestrianFromBus(e.id, e.position);
    }

    @Subscribe
    public void handle(PedestrianAgentDeadEvent e) {
        onHandle(e);
        webService.killPedestrian(e.id, e.travelDistance, e.travelTime);
    }

    private void onHandle(Object obj) {
        logger.info("Handling " + obj.getClass().getSimpleName());
    }

    private void onHandle(Object obj, String message) {
        logger.info("Handling " + obj.getClass().getSimpleName() + ". " + message);
    }
}
