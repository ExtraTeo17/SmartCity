package osmproxy.buses.abstractions;

import org.w3c.dom.Document;
import osmproxy.buses.data.BusPreparationData;
import osmproxy.elements.OSMWay;

import java.util.List;

public interface IBusDataParser {
    BusPreparationData parseBusData(Document busData);
    List<OSMWay> parseOsmWays(Document nodes);
}
