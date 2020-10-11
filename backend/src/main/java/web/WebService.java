package web;

import com.google.inject.Inject;
import routing.core.IGeoPosition;
import web.abstractions.IWebService;
import web.message.MessageType;
import web.message.payloads.infos.CreateCarInfo;
import web.message.payloads.infos.KillCarInfo;
import web.message.payloads.infos.UpdateCarInfo;
import web.message.payloads.models.Location;
import web.message.payloads.responses.PrepareResponse;
import web.message.payloads.responses.StartResponse;
import web.serialization.Converter;

import java.util.List;

class WebService implements IWebService {
    private final WebConnector webConnector;

    @Inject
    WebService(WebConnector webConnector) {
        this.webConnector = webConnector;
    }

    @Override
    public void start() {
        webConnector.start();
    }

    @Override
    public void prepareSimulation(List<? extends IGeoPosition> positions) {
        var locations = positions.stream().map(Converter::convert)
                .toArray(Location[]::new);
        var payload = new PrepareResponse(locations);
        webConnector.broadcastMessage(MessageType.PREPARE_SIMULATION_RESPONSE, payload);
    }

    @Override
    public void createCar(int id, IGeoPosition position, boolean isTestCar) {
        var location = Converter.convert(position);
        var payload = new CreateCarInfo(id, location, isTestCar);
        webConnector.broadcastMessage(MessageType.CREATE_CAR_INFO, payload);
    }

    @Override
    public void updateCar(int id, IGeoPosition position) {
        var location = Converter.convert(position);
        var payload = new UpdateCarInfo(id, location);
        webConnector.broadcastMessage(MessageType.UPDATE_CAR_INFO, payload);
    }

    @Override
    public void startSimulation(int timeScale) {
        var payload = new StartResponse(timeScale);
        webConnector.broadcastMessage(MessageType.START_SIMULATION_RESPONSE, payload);
    }

    @Override
    public void killCar(int id) {
        var payload = new KillCarInfo(id);
        webConnector.broadcastMessage(MessageType.KILL_CAR_INFO, payload);
    }
}
