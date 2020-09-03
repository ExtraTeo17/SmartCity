package osmproxy.buses;

import org.json.simple.JSONObject;
import org.w3c.dom.Document;

import java.util.Optional;

public interface IBusApiManager {
    Optional<Document> getBusDataXml();
    Optional<JSONObject> getNodesViaWarszawskieAPI(String busStopId, String busStopNr, String busLine);
}
