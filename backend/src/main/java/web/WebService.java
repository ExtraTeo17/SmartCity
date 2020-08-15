package web;

import com.google.inject.Inject;
import routing.IGeoPosition;
import web.abstractions.IWebService;
import web.message.MessageType;
import web.message.payloads.responses.Location;
import web.message.payloads.responses.SetZoneResponse;
import web.serialization.Converter;

import java.util.List;

public class WebService implements IWebService {
    private final WebConnector webConnector;

    @Inject
    public WebService(WebConnector webConnector) {
        this.webConnector = webConnector;
    }

    @Override
    public void start() {
        webConnector.start();
    }

    public void setZone(List<IGeoPosition> positions) {
        var locations = positions.stream().map(Converter::convert)
                .toArray(Location[]::new);
        var payload = new SetZoneResponse(locations);
        webConnector.broadcastMessage(MessageType.SET_ZONE_RESPONSE, payload);
    }
}
