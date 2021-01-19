package web;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import events.web.*;
import events.web.bike.*;
import events.web.bus.*;
import events.web.car.*;
import events.web.pedestrian.*;
import events.web.roadblocks.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import web.abstractions.IWebService;

/**
 * Used for handling all events which result in some interaction with frontend. <br/>
 * It is the highest layer of communication with frontend. <br/>
 */
@SuppressWarnings("OverlyCoupledClass")
class Communicator {
    private static final Logger logger = LoggerFactory.getLogger(Communicator.class);

    private final IWebService webService;

    @Inject
    public Communicator(IWebService webService) {
        this.webService = webService;
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
        webService.startSimulation();
    }

    @Subscribe
    public void handle(CarAgentCreatedEvent e) {
        onHandle(e, "Id: " + e.agentId);
        webService.createCar(e.agentId, e.agentPosition, e.route, e.isTestCar);
    }

    @Subscribe
    public void handle(CarAgentUpdatedEvent e) {
        webService.updateCar(e.id, e.position);
    }

    @Subscribe
    public void handle(SwitchLightsEvent e) {
        webService.updateLights(e.osmLightId);
    }

    @Subscribe
    public void handle(CarAgentDeadEvent e) {
        onHandle(e);
        webService.killCar(e.id, e.travelDistance, e.travelTime);
    }

    @Subscribe
    public void handle(CarAgentRouteChangedEvent e) {
        webService.changeCarRoute(e.agentId, e.routeStart, e.changePosition, e.routeEnd);
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
    public void handle(BusAgentUpdatedEvent e) {
        webService.updateBus(e.id, e.position);
    }

    @Subscribe
    public void handle(BusAgentFillStateUpdatedEvent e) {
        webService.updateBusFillState(e.id, e.fillState);
    }

    @Subscribe
    public void handle(BusAgentCrashedEvent e) {
        webService.crashBus(e.id);
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
        webService.pullPedestrianAwayFromBus(e.id, e.position, e.shouldShowRoute);
    }

    @Subscribe
    public void handle(PedestrianAgentDeadEvent e) {
        onHandle(e);
        webService.killPedestrian(e.id, e.travelDistance, e.travelTime);
    }

    @Subscribe
    public void handle(TrafficJamStartedEvent e) {
        onHandle(e);
        webService.startTrafficJam(e.lightId);
    }

    @Subscribe
    public void handle(TrafficJamFinishedEvent e) {
        onHandle(e);
        webService.endTrafficJam(e.lightId);
    }

    @Subscribe
    public void handle(BikeAgentCreatedEvent e) {
        onHandle(e);
        webService.createBike(e.agentId, e.agentPosition, e.route, e.isTestBike);
    }

    @Subscribe
    public void handle(BikeAgentUpdatedEvent e) {
        webService.updateBike(e.id, e.position);
    }

    @Subscribe
    public void handle(BikeAgentDeadEvent e) {
        onHandle(e);
        webService.killBike(e.id, e.travelDistance, e.travelTime);
    }

    @Subscribe
    public void handle(BatchedUpdateEvent e) {
        webService.batchedUpdate(e.carUpdates, e.bikeUpdates, e.busUpdates, e.pedUpdates);
    }

    @Subscribe
    public void handle(ApiOverloadedEvent e) {
        onHandle(e);
        webService.notifyApiOverload();
    }


    private void onHandle(Object obj) {
        logger.info("Handling " + obj.getClass().getSimpleName());
    }

    private void onHandle(Object obj, String message) {
        logger.info("Handling " + obj.getClass().getSimpleName() + ". " + message);
    }
}
