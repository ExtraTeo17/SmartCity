package web;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import events.LightManagersReadyEvent;
import events.web.VehicleAgentCreatedEvent;
import events.web.VehicleAgentUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import web.abstractions.IWebService;

import java.util.stream.Collectors;

class Communicator {
    private static final Logger logger = LoggerFactory.getLogger(Communicator.class);
    private final IWebService webService;

    @Inject
    public Communicator(IWebService webService) {
        this.webService = webService;
    }

    @Subscribe
    public void handle(LightManagersReadyEvent e) {
        onHandle(e);
        var positions = e.lightManagers.stream()
                .flatMap(man -> man.getLights().stream())
                .collect(Collectors.toList());
        webService.prepareSimulation(positions);
    }

    @Subscribe
    public void handle(VehicleAgentCreatedEvent e) {
        onHandle(e);
        webService.createCar(e.agentId, e.agentPosition, e.isTestCar);
    }

    @Subscribe
    public void handle(VehicleAgentUpdatedEvent e) {
        webService.updateCar(e.agentId, e.agentPosition);
    }

    private void onHandle(Object obj) {
        logger.info("Handling " + obj.getClass().getSimpleName());
    }
}
