package web;

import com.google.inject.Inject;
import routing.core.IGeoPosition;
import web.abstractions.IWebService;
import web.message.MessageType;
import web.message.payloads.infos.CreateCarInfo;
import web.message.payloads.models.Location;
import web.message.payloads.responses.SetZoneResponse;
import web.serialization.Converter;

import java.util.List;

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

    public void setZone(List<IGeoPosition> positions) {
        var locations = positions.stream().map(Converter::convert)
                .toArray(Location[]::new);
        var payload = new SetZoneResponse(locations);
        webConnector.broadcastMessage(MessageType.SET_ZONE_RESPONSE, payload);
    }

    @Override
    public void createCar(IGeoPosition position) {
        var location = Converter.convert(position);
        var payload = new CreateCarInfo(location);
        webConnector.broadcastMessage(MessageType.CREATE_CAR_INFO, payload);
    }
}
