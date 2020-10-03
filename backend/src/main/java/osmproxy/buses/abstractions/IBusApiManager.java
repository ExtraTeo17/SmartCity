package osmproxy.buses.abstractions;

import org.w3c.dom.Document;
import routing.core.IZone;

import java.util.List;
import java.util.Optional;

public interface IBusApiManager {
    Optional<Document> getBusDataXml(IZone zone);

    Optional<String> getBusTimetablesViaWarszawskieAPI(String busStopId, String busStopNr, String busLine);

    Optional<Document> getBusWays(List<Long> waysIds);
}
