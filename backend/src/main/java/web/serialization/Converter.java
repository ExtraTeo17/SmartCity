package web.serialization;


import routing.core.IGeoPosition;
import smartcity.lights.core.Light;
import web.message.payloads.models.LightDto;
import web.message.payloads.models.Location;

public class Converter {
    public static Location convert(IGeoPosition geoPosition) {
        return new Location(geoPosition.getLat(), geoPosition.getLng());
    }

    public static LightDto convert(Light light) {
        return new LightDto(light.getOsmLightId(), convert((IGeoPosition)light));
    }
}
