package mocks;

import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import osmproxy.buses.abstractions.IBusApiManager;
import routing.core.IZone;

import java.util.Optional;

public class BusApiManagerMock implements IBusApiManager {
    @Override
    public Optional<Document> getBusDataXml(IZone zone) {
        return Optional.empty();
    }

    @Override
    public Optional<JSONObject> getNodesViaWarszawskieAPI(String busStopId, String busStopNr, String busLine) {
        return Optional.empty();
    }

    @Override
    public Optional<Document> getBusWays(String query) {
        return Optional.empty();
    }
}
