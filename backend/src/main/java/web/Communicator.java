package web;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import events.web.*;
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
        onHandle(e, "Lights: " + e.lights.size() + ", stations: " + e.stations.size());
        webService.prepareSimulation(e.lights, e.stations);
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
        webService.updateCar(e.agentId, e.agentPosition);
    }

    @Subscribe
    public void handle(SwitchLightsEvent e) {
        webService.updateLights(e.osmLightId);
    }

    @Subscribe
    public void handle(VehicleAgentDeadEvent e) {
        onHandle(e);
        webService.killCar(e.id);
    }

    @Subscribe
    public void handle(VehicleAgentRouteChangedEvent e) {
        webService.changeRoute(e.agentId, e.route, e.changePosition);
    }

    @Subscribe
    public void handle(TroublePointCreatedEvent e) {
        webService.createTroublePoint(e.id, e.position);
    }

    private void onHandle(Object obj) {
        logger.info("Handling " + obj.getClass().getSimpleName());
    }

    private void onHandle(Object obj, String message) {
        logger.info("Handling " + obj.getClass().getSimpleName() + ". " + message);
    }
}
