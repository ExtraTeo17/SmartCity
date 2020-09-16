package osmproxy.buses;

import org.junit.Test;
import testutils.XmlParser;

class BusDataParserTests {

    @Test
    public void parseBusData() {
        var document = XmlParser.getDocument("DefaultBusZoneData.xml");
    }
}