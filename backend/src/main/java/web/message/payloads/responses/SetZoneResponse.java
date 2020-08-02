package web.message.payloads.responses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jxmapviewer.viewer.GeoPosition;
import web.message.payloads.AbstractPayload;

public class SetZoneResponse extends AbstractPayload {
    public final GeoPosition[] coordinates;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public SetZoneResponse(@JsonProperty("coordinates") GeoPosition[] coordinates) {
        this.coordinates = coordinates;
    }
}
