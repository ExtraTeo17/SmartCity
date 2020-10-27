package web;

import com.google.inject.Inject;
import osmproxy.elements.OSMNode;
import routing.core.IGeoPosition;
import smartcity.lights.core.Light;
import vehicles.Bus;
import vehicles.enums.BusFillState;
import web.abstractions.IWebService;
import web.message.MessageType;
import web.message.payloads.infos.create.CreateCarInfo;
import web.message.payloads.infos.create.CreatePedestrianInfo;
import web.message.payloads.infos.create.CreateTroublePointInfo;
import web.message.payloads.infos.kill.HideTroublePointInfo;
import web.message.payloads.infos.kill.KillBusInfo;
import web.message.payloads.infos.kill.KillCarInfo;
import web.message.payloads.infos.kill.KillPedestrianInfo;
import web.message.payloads.infos.update.*;
import web.message.payloads.models.BusDto;
import web.message.payloads.models.LightDto;
import web.message.payloads.models.Location;
import web.message.payloads.models.StationDto;
import web.message.payloads.responses.PrepareResponse;
import web.message.payloads.responses.StartResponse;
import web.serialization.Converter;

import java.util.List;

@SuppressWarnings("OverlyCoupledClass")
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
    public void prepareSimulation(List<? extends Light> lights,
                                  List<? extends OSMNode> stations,
                                  List<? extends Bus> buses) {
        var lightDtos = lights.stream().map(Converter::convert)
                .toArray(LightDto[]::new);
        var stationDtos = stations.stream().map(Converter::convert)
                .toArray(StationDto[]::new);
        var busDtos = buses.stream().map(Converter::convert).
                toArray(BusDto[]::new);
        var payload = new PrepareResponse(lightDtos, stationDtos, busDtos);

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
    public void killCar(int id, int travelDistance, Long travelTime) {
        var payload = new KillCarInfo(id, travelDistance, travelTime);

        webConnector.broadcastMessage(MessageType.KILL_CAR_INFO, payload);
    }

    @Override
    public void createTroublePoint(int id, IGeoPosition position) {
        var location = Converter.convert(position);
        var payload = new CreateTroublePointInfo(id, location);

        webConnector.broadcastMessage(MessageType.CREATE_TROUBLE_POINT_INFO, payload);
    }

    @Override
    public void hideTroublePoint(int id) {
        var payload = new HideTroublePointInfo(id);

        webConnector.broadcastMessage(MessageType.HIDE_TROUBLE_POINT_INFO, payload);
    }

    @Override
    public void changeRoute(int agentId,
                            List<? extends IGeoPosition> routeStart,
                            IGeoPosition changePosition,
                            List<? extends IGeoPosition> routeEnd) {
        var changeLocation = Converter.convert(changePosition);
        var routeStartLocations = routeStart.stream().map(Converter::convert).toArray(Location[]::new);
        var routeEndLocations = routeEnd.stream().map(Converter::convert).toArray(Location[]::new);
        var payload = new ChangeCarRouteInfo(agentId, routeStartLocations, changeLocation, routeEndLocations);

        webConnector.broadcastMessage(MessageType.UPDATE_CAR_ROUTE_INFO, payload);
    }

    @Override
    public void updateBus(int id, IGeoPosition position) {
        var location = Converter.convert(position);
        var payload = new UpdateBusInfo(id, location);

        webConnector.broadcastMessage(MessageType.UPDATE_BUS_INFO, payload);
    }

    @Override
    public void updateBusFillState(int id, BusFillState fillState) {
        var fillStateDto = Converter.convert(fillState);
        var payload = new UpdateBusFillStateInfo(id, fillStateDto);

        webConnector.broadcastMessage(MessageType.UPDATE_BUS_FILL_STATE_INFO, payload);
    }

    @Override
    public void killBus(int id) {
        var payload = new KillBusInfo(id);

        webConnector.broadcastMessage(MessageType.KILL_BUS_INFO, payload);
    }

    @Override
    public void createPedestrian(int id,
                                 IGeoPosition position,
                                 List<? extends IGeoPosition> routeToStation,
                                 List<? extends IGeoPosition> routeFromStation,
                                 boolean isTestPedestrian) {
        var location = Converter.convert(position);
        var routeToStationLocations = routeToStation.stream().map(Converter::convert).toArray(Location[]::new);
        var routeFromStationLocations = routeFromStation.stream().map(Converter::convert).toArray(Location[]::new);
        var payload = new CreatePedestrianInfo(id, location, routeFromStationLocations,
                routeToStationLocations,
                isTestPedestrian);

        webConnector.broadcastMessage(MessageType.CREATE_PEDESTRIAN_INFO, payload);
    }

    @Override
    public void updatePedestrian(int id, IGeoPosition position) {
        var location = Converter.convert(position);
        var payload = new UpdatePedestrianInfo(id, location);

        webConnector.broadcastMessage(MessageType.UPDATE_PEDESTRIAN_INFO, payload);
    }

    @Override
    public void pushPedestrianIntoBus(int id) {
        var payload = new PushPedestrianIntoBusInfo(id);

        webConnector.broadcastMessage(MessageType.PUSH_PEDESTRIAN_INTO_BUS_INFO, payload);
    }

    @Override
    public void pullPedestrianFromBus(int id, IGeoPosition position) {
        var location = Converter.convert(position);
        var payload = new PullPedestrianInfo(id, location);

        webConnector.broadcastMessage(MessageType.PULL_PEDESTRIAN_FROM_BUS_INFO, payload);
    }

    @Override
    public void killPedestrian(int id, int travelDistance, Long travelTime) {
        var payload = new KillPedestrianInfo(id, travelDistance, travelTime);

        webConnector.broadcastMessage(MessageType.KILL_PEDESTRIAN_INFO, payload);
    }
}
