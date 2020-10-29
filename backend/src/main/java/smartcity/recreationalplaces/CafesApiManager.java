package smartcity.recreationalplaces;


import com.google.inject.Inject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import osmproxy.OsmQueryManager;
import osmproxy.abstractions.IMapAccessManager;
import osmproxy.buses.data.BusInfoData;
import osmproxy.buses.data.BusPreparationData;
import osmproxy.elements.OSMStation;
import routing.core.IZone;
import utilities.ConditionalExecutor;
import utilities.FileWrapper;
import utilities.IterableNodeList;

import java.util.*;

public class CafesApiManager implements ICafesApiManager {
    private static final Logger logger = LoggerFactory.getLogger(osmproxy.buses.BusApiManager.class);
    private static final JSONParser jsonParser = new JSONParser();

    private final IMapAccessManager mapAccessManager;

    @Inject
    public CafesApiManager(IMapAccessManager mapAccessManager) {
        this.mapAccessManager = mapAccessManager;
    }

    @Override
    public Optional<Document> getCafesDataXml(IZone zone) {
        var query = OsmQueryManager.getCoffeeAroundPointQuery(zone.getCenter().getLat(),
                                                                zone.getCenter().getLng(), zone.getRadius());
        var overpassInfo = mapAccessManager.getNodesDocument(query);
        Set<OSMCafe> cafes = parseCafeInfo(overpassInfo.get());
        ConditionalExecutor.debug(() -> {
            logger.info("Writing cafes-data to: " + FileWrapper.DEFAULT_OUTPUT_PATH_XML);
            //noinspection OptionalGetWithoutIsPresent
            FileWrapper.write(overpassInfo.get());
        }, overpassInfo.isPresent());

        return overpassInfo;
    }

    public Set<OSMCafe> parseCafeInfo(Document cafeInfo) {
        Node osmRoot = cafeInfo.getFirstChild();
        var osmXMLNodes = IterableNodeList.of(osmRoot.getChildNodes());

        Set<OSMCafe> cafes = new HashSet<>();
       // HashMap<Long, OSMStation> busStopsMap = new LinkedHashMap<>();
        int errors = 0;
        for (var osmNode : osmXMLNodes) {
            var nodeName = osmNode.getNodeName();
            if (nodeName.equals("node")) {
                NamedNodeMap attributes = osmNode.getAttributes();
                long osmId = Long.parseLong(attributes.getNamedItem("id").getNodeValue());
                double lat = Double.parseDouble(attributes.getNamedItem("lat").getNodeValue());
                double lon = Double.parseDouble(attributes.getNamedItem("lon").getNodeValue());


                cafes.add(new OSMCafe(osmId,lat,lon));
            }

        }

        return cafes;

    }


}
