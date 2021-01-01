package web;

import com.google.inject.Inject;
import events.web.models.UpdateObject;
import osmproxy.elements.OSMStation;
import routing.core.IGeoPosition;
import smartcity.lights.core.Light;
import vehicles.Bus;
import vehicles.enums.BusFillState;
import web.abstractions.IWebService;
import web.message.MessageType;
import web.message.payloads.infos.create.*;
import web.message.payloads.infos.kill.*;
import web.message.payloads.infos.other.ApiOverloadInfo;
import web.message.payloads.infos.other.ChangeCarRouteInfo;
import web.message.payloads.infos.other.CrashBusInfo;
import web.message.payloads.infos.other.SwitchLightsInfo;
import web.message.payloads.infos.update.*;
import web.message.payloads.models.*;
import web.message.payloads.responses.PrepareResponse;
import web.message.payloads.responses.StartResponse;
import web.serialization.Converter;

import java.util.List;


/**
 * Used for interaction with frontend interface.
 * Contains all method necessary to pass data or notify GUI.
 * Each method has it's own payload (which extends {@link web.message.payloads.AbstractPayload} and {@link MessageType} <br/>
 * Class is responsible for types conversion and {@link MessageType} assigment. <br/>
 */
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
                                  List<? extends OSMStation> stations,
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

    public void startSimulation() {
        var payload = new StartResponse();
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
    public void changeCarRoute(int agentId,
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
    public void crashBus(int id) {
        var payload = new CrashBusInfo(id);

        webConnector.broadcastMessage(MessageType.CRASH_BUS_INFO, payload);
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
    public void pullPedestrianAwayFromBus(int id, IGeoPosition position, boolean showRoute) {
        var location = Converter.convert(position);
        var payload = new PullPedestrianAwayFromBusInfo(id, location, showRoute);

        webConnector.broadcastMessage(MessageType.PULL_PEDESTRIAN_AWAY_FROM_BUS_INFO, payload);
    }

    @Override
    public void killPedestrian(int id, int travelDistance, Long travelTime) {
        var payload = new KillPedestrianInfo(id, travelDistance, travelTime);

        webConnector.broadcastMessage(MessageType.KILL_PEDESTRIAN_INFO, payload);
    }

    @Override
    public void startTrafficJam(long id) {
        var payload = new StartTrafficJamInfo(id);

        webConnector.broadcastMessage(MessageType.START_TRAFFIC_JAM_INFO, payload);
    }

    @Override
    public void endTrafficJam(long id) {
        var payload = new EndTrafficJamInfo(id);

        webConnector.broadcastMessage(MessageType.END_TRAFFIC_JAM_INFO, payload);
    }

    @Override
    public void createBike(int id, IGeoPosition position, List<? extends IGeoPosition> route, boolean isTestBike) {
        var location = Converter.convert(position);
        var routeLocations = route.stream().map(Converter::convert).toArray(Location[]::new);
        var payload = new CreateBikeInfo(id, location, routeLocations, isTestBike);

        webConnector.broadcastMessage(MessageType.CREATE_BIKE_INFO, payload);
    }

    @Override
    public void updateBike(int id, IGeoPosition position) {
        var location = Converter.convert(position);
        var payload = new UpdateBikeInfo(id, location);

        webConnector.broadcastMessage(MessageType.UPDATE_BIKE_INFO, payload);
    }

    @Override
    public void killBike(int id, int travelDistance, Long travelTime) {
        var payload = new KillBikeInfo(id, travelDistance, travelTime);

        webConnector.broadcastMessage(MessageType.KILL_BIKE_INFO, payload);
    }

    @Override
    public void batchedUpdate(List<UpdateObject> carUpdates,
                              List<UpdateObject> bikeUpdates,
                              List<UpdateObject> busUpdates,
                              List<UpdateObject> pedUpdates) {
        var carUpdateDtos = carUpdates.stream().map(Converter::convert).toArray(UpdateDto[]::new);
        var bikeUpdateDtos = bikeUpdates.stream().map(Converter::convert).toArray(UpdateDto[]::new);
        var busUpdateDtos = busUpdates.stream().map(Converter::convert).toArray(UpdateDto[]::new);
        var pedUpdateDtos = pedUpdates.stream().map(Converter::convert).toArray(UpdateDto[]::new);
        var payload = new BatchedUpdateInfo(carUpdateDtos, bikeUpdateDtos, busUpdateDtos, pedUpdateDtos);

        webConnector.broadcastMessage(MessageType.BATCHED_UPDATE_INFO, payload);
    }

    @Override
    public void notifyApiOverload() {
        var payload = new ApiOverloadInfo();
        webConnector.broadcastMessage(MessageType.API_OVERLOAD_INFO, payload);
    }
}
