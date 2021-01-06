package osmproxy.buses.abstractions;

import org.w3c.dom.Document;
import routing.core.IZone;

import java.util.List;
import java.util.Optional;

/**
 * Connects with external API, in order to get bus data
 */
public interface IBusApiManager {
    Optional<Document> getBusDataXml(IZone zone);

    Optional<String> getBusTimetablesViaWarszawskieAPI(String busStopId, String busStopNr, String busLine);

    Optional<Document> getBusWays(List<Long> waysIds);
}
