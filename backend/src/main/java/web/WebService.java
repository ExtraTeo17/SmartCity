package web;

import com.google.inject.Inject;
import routing.core.IGeoPosition;
import smartcity.lights.core.Light;
import web.abstractions.IWebService;
import web.message.MessageType;
import web.message.payloads.infos.CreateCarInfo;
import web.message.payloads.infos.SwitchLightsInfo;
import web.message.payloads.infos.UpdateCarInfo;
import web.message.payloads.models.LightDto;
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
    public void prepareSimulation(List<? extends Light> positions) {
        var lights = positions.stream().map(Converter::convert)
                .toArray(LightDto[]::new);
        var payload = new PrepareResponse(lights);
        webConnector.broadcastMessage(MessageType.PREPARE_SIMULATION_RESPONSE, payload);
    }

    @Override
    public void startSimulation(int timeScale) {
        var payload = new StartResponse(timeScale);
        webConnector.broadcastMessage(MessageType.START_SIMULATION_RESPONSE, payload);
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
    public void updateLights(long lightGroupId) {
        var payload = new SwitchLightsInfo(lightGroupId);
        webConnector.broadcastMessage(MessageType.SWITCH_LIGHTS_INFO, payload);
    }
}
