package osmproxy.buses.abstractions;

import org.w3c.dom.Document;
import osmproxy.buses.data.BusPreparationData;

public interface IBusDataParser {
    BusPreparationData parseBusData(Document busData);
}
