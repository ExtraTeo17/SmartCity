package web;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import events.LightManagersReadyEvent;
import events.web.SimulationStartedEvent;
import events.web.VehicleAgentCreatedEvent;
import events.web.VehicleAgentDeadEvent;
import events.web.VehicleAgentUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartcity.ITimeProvider;
import smartcity.TimeProvider;
import web.abstractions.IWebService;

import java.util.stream.Collectors;

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


    // TODO: Should probably be changed to SimulationPreparedEvent
    @Subscribe
    public void handle(LightManagersReadyEvent e) {
        onHandle(e);
        var positions = e.lightManagers.stream()
                .flatMap(man -> man.getLights().stream())
                .collect(Collectors.toList());
        webService.prepareSimulation(positions);
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
    public void handle(VehicleAgentDeadEvent e){
        onHandle(e);
        webService.killCar(e.id);
    }

    private void onHandle(Object obj) {
        logger.info("Handling " + obj.getClass().getSimpleName());
    }
}
