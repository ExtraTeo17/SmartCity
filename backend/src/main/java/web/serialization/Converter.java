package web.serialization;

import org.jxmapviewer.viewer.GeoPosition;
import web.message.payloads.responses.Location;

public class Converter {
    public static Location convert(GeoPosition geoPosition){
        return new Location(geoPosition.getLongitude(), geoPosition.getLatitude());
    }
}
