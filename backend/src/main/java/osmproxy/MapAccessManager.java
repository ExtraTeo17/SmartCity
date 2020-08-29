/*
  (c) Jens Kbler
  This software is public domain
  <p>
  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package osmproxy;

import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import osmproxy.elements.OSMLight;
import osmproxy.elements.OSMNode;
import osmproxy.elements.OSMWay;
import routing.RouteInfo;
import routing.core.IZone;
import routing.core.Position;
import utilities.IterableNodeList;
import utilities.NumericHelper;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

//import org.osm.lights.diff.OSMNode;
//import org.osm.lights.upload.BasicAuthenticator;

/**
 *
 */
public class MapAccessManager {
    private static final Logger logger = LoggerFactory.getLogger(MapAccessManager.class);
    private static final String OVERPASS_API = "https://lz4.overpass-api.de/api/interpreter";
    private static final String CROSSROADS_LOCATIONS_PATH = "config/crossroads.xml";


    /**
     * @return a list of openseamap nodes extracted from xml
     */
    @SuppressWarnings("nls")

    static List<OSMNode> getNodes(Document xmlDocument) {
        List<OSMNode> osmNodes = new ArrayList<>();
        Node osmRoot = xmlDocument.getFirstChild();
        NodeList osmXMLNodes = osmRoot.getChildNodes();
        for (int i = 1; i < osmXMLNodes.getLength(); i++) {
            Node item = osmXMLNodes.item(i);
            if (item.getNodeName().equals("node")) {
                var args = getNodeArgs(item);
                osmNodes.add(new OSMNode(args.getValue0(), args.getValue1(), args.getValue2()));
            }
        }
        return osmNodes;
    }

    /**
     * @return (id, lat, lng)
     */
    private static Triplet<String, String, String> getNodeArgs(Node xmlNode) {
        NamedNodeMap attributes = xmlNode.getAttributes();
        String id = attributes.getNamedItem("id").getNodeValue();
        String latitude = attributes.getNamedItem("lat").getNodeValue();
        String longitude = attributes.getNamedItem("lon").getNodeValue();

        return Triplet.with(id, latitude, longitude);
    }

    private static List<OSMLight> parseLights(Document xmlDocument) {
        Node osmRoot = xmlDocument.getFirstChild();
        IterableNodeList osmXMLNodes = IterableNodeList.of(osmRoot.getChildNodes());
        List<OSMLight> osmLights = new ArrayList<>();
        int firstNodeIndex = 0;
        for (var xmlNode : osmXMLNodes) {
            NamedNodeMap attributes = xmlNode.getAttributes();
            if (xmlNode.getNodeName().equals("node")) {
                String id = attributes.getNamedItem("id").getNodeValue();
                String lat = attributes.getNamedItem("lat").getNodeValue();
                String lon = attributes.getNamedItem("lon").getNodeValue();
                osmLights.add(new OSMLight(id, lat, lon));
            }
            else if (xmlNode.getNodeName().equals("way")) {
                String adherentWayId = attributes.getNamedItem("id").getNodeValue();
                for (int i = firstNodeIndex; i < osmLights.size(); ++i) {
                    osmLights.get(i).setAdherentWayId(adherentWayId);
                }
                firstNodeIndex = osmLights.size();
            }
        }

        return osmLights;
    }

    private static RouteInfo parseWayAndNodes(Document nodesViaOverpass) {
        final RouteInfo info = new RouteInfo();
        Node osmRoot = nodesViaOverpass.getFirstChild();
        NodeList osmXMLNodes = osmRoot.getChildNodes();
        for (int i = 1; i < osmXMLNodes.getLength(); i++) {
            Node item = osmXMLNodes.item(i);
            if (item.getNodeName().equals("way")) {
                info.addWay(new OSMWay(item));
            }
            else if (item.getNodeName().equals("node")) { // TODO: for further future: add support for rare way-traffic-signal-crossings cases
                NodeList nodeChildren = item.getChildNodes();
                for (int j = 0; j < nodeChildren.getLength(); ++j) {
                    Node nodeChild = nodeChildren.item(j);
                    if (nodeChild.getNodeName().equals("tag") &&
                            nodeChild.getAttributes().getNamedItem("k").getNodeValue().equals("crossing") &&
                            nodeChild.getAttributes().getNamedItem("v").getNodeValue().equals("traffic_signals")) {
                        info.add(item.getAttributes().getNamedItem("id").getNodeValue());
                    }
                }
            }
        }
        return info;
    }

    // TODO: Remove checked exceptions from here

    /**
     * @param query the overpass query
     * @return the nodes in the formulated query
     */
    public static Document getNodesViaOverpass(String query) {
        HttpURLConnection connection = getConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        try {
            DataOutputStream printout = new DataOutputStream(connection.getOutputStream());
            printout.writeBytes("data=" + URLEncoder.encode(query, StandardCharsets.UTF_8));
            printout.flush();
            printout.close();
        } catch (IOException e) {
            logger.warn("Error getting data from connection");
            throw new RuntimeException(e);
        }

        // TODO: Cache builder
        try {
            DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
            return docBuilder.parse(connection.getInputStream());
        } catch (IOException | ParserConfigurationException | SAXException e) {
            logger.error("Error parsing data from connection");
            throw new RuntimeException(e);
        }
    }


    private static HttpURLConnection getConnection() {
        URL url;
        try {
            url = new URL(OVERPASS_API);
        } catch (MalformedURLException e) {
            logger.error("Error creating url: " + OVERPASS_API);
            throw new RuntimeException(e);
        }

        try {
            return (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            logger.error("Error opening connection to " + OVERPASS_API);
            throw new RuntimeException(e);
        }
    }


    public static List<OSMLight> sendFullTrafficSignalQuery(List<Long> osmWayIds) {
        List<OSMLight> lightNodes = new ArrayList<>();
        try {
            var overpassNodes = getNodesViaOverpass(OsmQueryManager.getFullTrafficSignalQuery(osmWayIds));
            lightNodes = parseLights(overpassNodes);
        } catch (Exception e) {
            logger.error("Error trying to get light nodes", e);
        }
        return lightNodes;
    }

    public static RouteInfo sendMultipleWayAndItsNodesQuery(List<Long> osmWayIds) {
        RouteInfo info = null;
        try {
            var overpassNodes = getNodesViaOverpass(OsmQueryManager.getMultipleWayAndItsNodesQuery(osmWayIds));
            info = parseWayAndNodes(overpassNodes);
        } catch (Exception e) {
            logger.warn("Error trying to get route info", e);
        }
        return info;
    }

    public static List<Node> getLightManagersNodes(IZone zone) {
        var lightManagersNodes = new ArrayList<Node>();
        Document xmlDocument = getXmlDocument(CROSSROADS_LOCATIONS_PATH);
        Node osmRoot = xmlDocument.getFirstChild();
        var districtXMLNodes = IterableNodeList.of(osmRoot.getChildNodes());
        try {
            for (var districtNode : districtXMLNodes) {
                if (districtNode.getNodeName().equals("district")) {
                    Node crossroadsRoot = districtNode.getChildNodes().item(1);
                    var crossroadXMLNodes = IterableNodeList.of(crossroadsRoot.getChildNodes());
                    for (var crossroad : crossroadXMLNodes) {
                        if (crossroad.getNodeName().equals("crossroad")) {
                            var crossroadPos = calculateLatLonBasedOnInternalLights(crossroad);
                            if (zone.contains(crossroadPos)) {
                                lightManagersNodes.add(crossroad);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Error preparing light managers", e);
            return lightManagersNodes;
        }

        return lightManagersNodes;
    }

    public static void parseChildNodesOfWays(Document childNodesOfWays, List<OSMNode> lightsOfTypeA) {
        Node osmRoot = childNodesOfWays.getFirstChild();
        NodeList osmXMLNodes = osmRoot.getChildNodes();
        int lightIndex = 0, parentWayIndex = 0;
        for (int i = 1; i < osmXMLNodes.getLength(); i++) {
            final Node item = osmXMLNodes.item(i);
            switch (item.getNodeName()) {
                case "node" -> {
                    String id = item.getAttributes().getNamedItem("id").getNodeValue();
                    lightsOfTypeA.get(lightIndex).addChildNodeIdForParentWay(parentWayIndex, id);
                }
                case "relation" -> ++parentWayIndex;
                case "way" -> {
                    ++lightIndex;
                    parentWayIndex = 0;
                }
            }
        }
    }

    private static Position calculateLatLonBasedOnInternalLights(Node crossroad) {
        var crossroadA = getCrossroadGroup(crossroad, 1);
        var crossroadB = getCrossroadGroup(crossroad, 3);
        List<Double> latList = getParametersFromGroup(crossroadA, crossroadB, "lat");
        List<Double> lonList = getParametersFromGroup(crossroadA, crossroadB, "lon");
        double latAverage = NumericHelper.calculateAverage(latList);
        double lonAverage = NumericHelper.calculateAverage(lonList);
        return Position.of(latAverage, lonAverage);
    }

    private static List<Double> getParametersFromGroup(Node group1, Node group2, String parameterName) {
        List<Double> parameterList = new ArrayList<>();
        addLightParametersFromGroup(parameterList, group1, parameterName);
        addLightParametersFromGroup(parameterList, group2, parameterName);
        return parameterList;
    }

    private static void addLightParametersFromGroup(List<Double> list, Node group, String parameterName) {
        NodeList lightNodes = group.getChildNodes();
        for (int i = 0; i < lightNodes.getLength(); ++i) {
            if (lightNodes.item(i).getNodeName().equals("light")) {
                list.add(Double.parseDouble(lightNodes.item(i).getAttributes().getNamedItem(parameterName).getNodeValue()));
            }
        }
    }

    public static Node getCrossroadGroup(Node crossroad, int index) {
        return crossroad.getChildNodes().item(index);
    }

    private static Document getXmlDocument(String filepath) {
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        Document document = null;
        try {
            DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
            document = docBuilder.parse(new File(filepath));
        } catch (SAXException e) {
            logger.warn("Error parsing xml.", e);
        } catch (IOException e) {
            logger.warn("Error accessing file.", e);
        } catch (ParserConfigurationException e) {
            logger.warn("Wrong parser configuration.", e);
        }

        return document;
    }

    public static List<OSMWay> parseOsmWay(Document nodesViaOverpass, IZone zone) {
        List<OSMWay> route = new ArrayList<>();
        Node osmRoot = nodesViaOverpass.getFirstChild();
        NodeList osmXMLNodes = osmRoot.getChildNodes();
        Pair<OSMWay, String> wayAdjacentNodeRef = determineInitialWayRelOrientation(osmXMLNodes);
        String adjacentNodeRef = wayAdjacentNodeRef.getValue1();
        boolean isFirst = true;
        for (int i = 1; i < osmXMLNodes.getLength(); i++) {
            Node item = osmXMLNodes.item(i);
            if (item.getNodeName().equals("way")) {
                OSMWay way;
                if (isFirst) {
                    way = wayAdjacentNodeRef.getValue0();
                    isFirst = false;
                }
                else {
                    way = new OSMWay(item);
                    try {
                        adjacentNodeRef = way.determineRelationOrientation(adjacentNodeRef);
                    } catch (UnsupportedOperationException e) {
                        logger.warn("Failed to determine orientation", e);
                        break;
                    }
                }

                // TODO: CORRECT POTENTIAL BUGS CAUSING ROUTE TO BE CUT INTO PIECES BECAUSE OF RZĄŻEWSKI CASE
                if (way.startsInZone(zone)) {
                    route.add(way);
                }
            }
        }
        return route;
    }

    // TODO: Is it returning orientation of next way or current way?
    private static Pair<OSMWay, String> determineInitialWayRelOrientation(final NodeList osmXMLNodes) {
        OSMWay firstWay = null;
        OSMWay lastWay = null;
        for (int it = 1; it < osmXMLNodes.getLength(); ++it) {
            Node node = osmXMLNodes.item(it);
            if (node.getNodeName().equals("way")) {
                var way = new OSMWay(node);
                if (firstWay == null) {
                    firstWay = way;
                }
                else {
                    lastWay = way;
                    break;
                }
            }
        }

        if (firstWay != null && lastWay != null) {
            var orientation = firstWay.determineRelationOrientation(lastWay);
            return Pair.with(lastWay, orientation);
        }

        throw new NoSuchElementException("Did not find two 'way'-type nodes in provided list.");
    }
}
