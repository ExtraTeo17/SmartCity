package web.serialization;


import agents.utilities.LightColor;
import routing.core.IGeoPosition;
import smartcity.lights.core.Light;
import web.message.payloads.models.LightColorDto;
import web.message.payloads.models.LightDto;
import web.message.payloads.models.Location;

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
}
