package osmproxy.buses;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import osmproxy.buses.abstractions.IBusApiManager;
import osmproxy.buses.abstractions.IBusDataParser;
import osmproxy.buses.abstractions.IBusLinesManager;
import osmproxy.buses.data.BusPreparationData;
import routing.core.IZone;

public class BusLinesManager implements IBusLinesManager {
    private static final Logger logger = LoggerFactory.getLogger(BusLinesManager.class);

    private final IBusApiManager busApiManager;
    private final IBusDataParser busDataParser;
    private final IZone zone;

    @Inject
    BusLinesManager(IBusApiManager busApiManager,
                    IBusDataParser busDataParser,
                    IZone zone) {
        this.busApiManager = busApiManager;
        this.busDataParser = busDataParser;
        this.zone = zone;
    }

    @Override
    public BusPreparationData getBusData() {
        var overpassInfo = busApiManager.getBusDataXml(zone);
        if (overpassInfo.isEmpty()) {
            return new BusPreparationData();
        }

        return busDataParser.parseBusData(overpassInfo.get());
    }
}
