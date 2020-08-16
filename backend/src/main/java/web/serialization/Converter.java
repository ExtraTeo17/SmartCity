package web.serialization;


import routing.IGeoPosition;
import web.message.payloads.responses.Location;

public class Converter {
    public static Location convert(IGeoPosition geoPosition) {
        return new Location(geoPosition.getLat(), geoPosition.getLng());
    }
}
