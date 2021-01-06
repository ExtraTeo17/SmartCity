package web.serialization;


import events.web.PrepareSimulationEvent;
import events.web.StartSimulationEvent;
import events.web.models.UpdateObject;
import osmproxy.elements.OSMNode;
import routing.core.IGeoPosition;
import smartcity.TimeProvider;
import smartcity.lights.LightColor;
import smartcity.lights.core.Light;
import vehicles.Bus;
import vehicles.enums.BusFillState;
import web.message.payloads.models.*;
import web.message.payloads.requests.PrepareSimulationRequest;
import web.message.payloads.requests.StartSimulationRequest;

/**
 * Used for backend &lt;-&gt; frontend type conversion.
 */
@SuppressWarnings("OverlyCoupledClass")
public class Converter {
    public static Location convert(IGeoPosition geoPosition) {
        return new Location(geoPosition.getLat(), geoPosition.getLng());
    }

    public static LightDto convert(Light light) {
        var id = light.uniqueId();
        var lightGroupId = light.getOsmLightId();
        var location = convert((IGeoPosition) light);
        var color = light.isGreen() ? LightColorDto.GREEN : LightColorDto.RED;

        return new LightDto(id, lightGroupId, location, color);
    }

    public static LightColorDto convert(LightColor lightColor) {
        return switch (lightColor) {
            case GREEN -> LightColorDto.GREEN;
            case YELLOW -> LightColorDto.YELLOW;
            case RED -> LightColorDto.RED;
        };
    }

    public static StationDto convert(OSMNode station) {
        var id = station.getId();
        var location = convert((IGeoPosition) station);

        return new StationDto(id, location);
    }

    public static BusDto convert(Bus bus) {
        var id = bus.getAgentId();
        var location = convert(bus.getPosition());
        var routeLocations = bus.getSimpleRoute().stream().map(Converter::convert).toArray(Location[]::new);
        var fillState = convert(bus.getFillState());

        return new BusDto(id, location, routeLocations, fillState);
    }

    public static BusFillStateDto convert(BusFillState fillState) {
        return switch (fillState) {
            case LOW -> BusFillStateDto.LOW;
            case MID -> BusFillStateDto.MID;
            case HIGH -> BusFillStateDto.HIGH;
        };
    }

    public static UpdateDto convert(UpdateObject updateObject) {
        return new UpdateDto(updateObject.id, convert(updateObject.position));
    }


    public static PrepareSimulationEvent convert(PrepareSimulationRequest req) {

        return new PrepareSimulationEvent(req.latitude, req.longitude, req.radius,
                req.generatePedestrians);
    }

    public static StartSimulationEvent convert(StartSimulationRequest req) {
        var timeLocal = TimeProvider.convertFromUtcToLocal(req.startTime).toLocalDateTime();

        return new StartSimulationEvent(
                req.generateCars,
                req.carsLimit,
                req.testCarId,
                req.generateBatchesForCars,

                req.generateBikes,
                req.bikesLimit,
                req.testBikeId,

                req.pedestriansLimit,
                req.testPedestrianId,

                req.generateTroublePoints,
                req.timeBeforeTrouble,

                req.generateBusFailures,
                req.detectTrafficJams,

                req.useFixedRoutes,
                req.useFixedTroublePoints,

                timeLocal,
                req.timeScale,

                req.lightStrategyActive,
                req.extendLightTime,

                req.stationStrategyActive,
                req.extendWaitTime,

                req.troublePointStrategyActive,
                req.troublePointThresholdUntilIndexChange,
                req.noTroublePointStrategyIndexFactor,

                req.trafficJamStrategyActive,
                req.transportChangeStrategyActive
        );
    }
}
