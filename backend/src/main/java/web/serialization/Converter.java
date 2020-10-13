package web.serialization;


import routing.core.IGeoPosition;
import web.message.payloads.models.Location;

public class Converter {
    public static Location convert(IGeoPosition geoPosition) {
        return new Location(geoPosition.getLat(), geoPosition.getLng());
    }
}
