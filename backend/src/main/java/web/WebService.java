package web;

import com.google.inject.Inject;
import osmproxy.elements.OSMNode;
import routing.core.IGeoPosition;
import smartcity.lights.core.Light;
import web.abstractions.IWebService;
import web.message.MessageType;
import web.message.payloads.infos.*;
import web.message.payloads.models.LightDto;
import web.message.payloads.models.Location;
import web.message.payloads.models.StationDto;
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
    public void prepareSimulation(List<? extends Light> lights, List<? extends OSMNode> stations) {
        var lightDtos = lights.stream().map(Converter::convert)
                .toArray(LightDto[]::new);
        var stationDtos = stations.stream().map(Converter::convert)
                .toArray(StationDto[]::new);
        var payload = new PrepareResponse(lightDtos, stationDtos);
        webConnector.broadcastMessage(MessageType.PREPARE_SIMULATION_RESPONSE, payload);
    }

    public void startSimulation(int timeScale) {
        var payload = new StartResponse(timeScale);
        webConnector.broadcastMessage(MessageType.START_SIMULATION_RESPONSE, payload);
    }

    @Override
    public void createCar(int id, IGeoPosition position, List<? extends IGeoPosition> route, boolean isTestCar) {
        var location = Converter.convert(position);
        var routeLocations = route.stream().map(Converter::convert).toArray(Location[]::new);
        var payload = new CreateCarInfo(id, location, routeLocations, isTestCar);
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

    @Override
    public void killCar(int id) {
        var payload = new KillCarInfo(id);
        webConnector.broadcastMessage(MessageType.KILL_CAR_INFO, payload);
    }

    @Override
    public void createTroublePoint(int id, IGeoPosition position) {
        var location = Converter.convert(position);
        var payload = new CreateTroublePointInfo(id, location);
        webConnector.broadcastMessage(MessageType.CREATE_TROUBLE_POINT_INFO, payload);
    }
}
