package web.serialization;


import agents.utilities.LightColor;
import osmproxy.elements.OSMNode;
import routing.core.IGeoPosition;
import smartcity.lights.core.Light;
import vehicles.Bus;
import web.message.payloads.models.*;

public class Converter {
    public static Location convert(IGeoPosition geoPosition) {
        return new Location(geoPosition.getLat(), geoPosition.getLng());
    }

    public static LightDto convert(Light light) {
        var lightGroupId = light.getOsmLightId();
        var location = convert((IGeoPosition) light);
        var color = light.isGreen() ? LightColorDto.GREEN : LightColorDto.RED;

        return new LightDto(lightGroupId, location, color);
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
        var location = convert((IGeoPosition) bus.getPosition());
        var routeLocations = bus.getSimpleRoute().stream().map(Converter::convert).toArray(Location[]::new);

        return new BusDto(id, location, routeLocations);
    }
}
