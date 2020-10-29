package recreationalplaces;


import com.google.inject.Inject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import osmproxy.OsmQueryManager;
import osmproxy.abstractions.IMapAccessManager;
import osmproxy.buses.abstractions.IBusApiManager;
import routing.core.IZone;
import utilities.ConditionalExecutor;
import utilities.FileWrapper;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class BusApiManager implements IBusApiManager {
    private static final Logger logger = LoggerFactory.getLogger(osmproxy.buses.BusApiManager.class);
    private static final JSONParser jsonParser = new JSONParser();

    private final IMapAccessManager mapAccessManager;

    @Inject
    public BusApiManager(IMapAccessManager mapAccessManager) {
        this.mapAccessManager = mapAccessManager;
    }

    @Override
    public Optional<Document> getBusDataXml(IZone zone) {
        var query = OsmQueryManager.getBusQuery(zone.getCenter(), zone.getRadius());
        var overpassInfo = mapAccessManager.getNodesDocument(query);

        ConditionalExecutor.debug(() -> {
            logger.info("Writing bus-data to: " + FileWrapper.DEFAULT_OUTPUT_PATH_XML);
            //noinspection OptionalGetWithoutIsPresent
            FileWrapper.write(overpassInfo.get());
        }, overpassInfo.isPresent());

        return overpassInfo;
    }




}
