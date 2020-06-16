/**
 * (c) Jens Kbler
 * This software is public domain
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package OSMProxy;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.graphhopper.util.shapes.GHPoint;

import Agents.LightManager;
import Agents.StationAgent;
import OSMProxy.Elements.OSMLight;
import OSMProxy.Elements.OSMNode;
import OSMProxy.Elements.OSMWay;
import OSMProxy.Elements.OSMStation;
import Routing.RouteNode;
import SmartCity.SmartCityAgent;
import SmartCity.Buses.BrigadeInfo;
import SmartCity.Buses.BusInfo;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.javatuples.Pair;
import org.jxmapviewer.viewer.GeoPosition;
//import org.osm.lights.diff.OSMNode;
//import org.osm.lights.upload.BasicAuthenticator;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.graphhopper.util.PointList;

/**
 * 
 */
public class MapAccessManager {
	
	protected static final String DELIMITER_RELATION = "3838046";
	protected static final String DELIMITER_WAY = "48439275";

	//private static final String OVERPASS_API = "http://www.overpass-api.de/api/interpreter";
	private static final String OVERPASS_API = "https://lz4.overpass-api.de/api/interpreter";

	private static final String OPENSTREETMAP_API_06 = "http://www.openstreetmap.org/api/0.6/";
	private static final String LAT = "lat";
	private static final String LON = "lon";
	private static final String ID = "id";
	private static final String CROSSROADS = "crossroads.xml";

	public static OSMNode getNode(String nodeId) throws IOException, ParserConfigurationException, SAXException {
		String string = "http://www.openstreetmap.org/api/0.6/node/" + nodeId;
		URL osm = new URL(string);
		HttpURLConnection connection = (HttpURLConnection) osm.openConnection();

		DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
		Document document = docBuilder.parse(connection.getInputStream());
		List<OSMNode> nodes = getNodes(document);
		if (!nodes.isEmpty()) {
			return nodes.iterator().next();
		}
		return null;
	}

	/**
	 * 
	 * @param lon the longitude
	 * @param lat the latitude
	 * @param vicinityRange bounding box in this range
	 * @return the xml document containing the queries nodes
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	@SuppressWarnings("nls")
	private static Document getXML(double lon, double lat, double vicinityRange) throws IOException, SAXException,
			ParserConfigurationException {

		DecimalFormat format = new DecimalFormat("##0.0000000", DecimalFormatSymbols.getInstance(Locale.ENGLISH)); //$NON-NLS-1$
		String left = format.format(lat - vicinityRange);
		String bottom = format.format(lon - vicinityRange);
		String right = format.format(lat + vicinityRange);
		String top = format.format(lon + vicinityRange);

		String string = OPENSTREETMAP_API_06 + "map?bbox=" + left + "," + bottom + "," + right + ","
				+ top;
		URL osm = new URL(string);
		HttpURLConnection connection = (HttpURLConnection) osm.openConnection();

		DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
		
		InputStream inp = connection.getInputStream();
		return docBuilder.parse(inp);
	}

	public static Document getXMLFile(String location) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
		return docBuilder.parse(location);
	}

	/**
	 * 
	 * @param xmlDocument 
	 * @return a list of openseamap nodes extracted from xml
	 */
	@SuppressWarnings("nls")
	public static List<OSMNode> getNodes(Document xmlDocument) {
		List<OSMNode> osmNodes = new ArrayList<OSMNode>();
		Node osmRoot = xmlDocument.getFirstChild();
		NodeList osmXMLNodes = osmRoot.getChildNodes();
		for (int i = 1; i < osmXMLNodes.getLength(); i++) {
			Node item = osmXMLNodes.item(i);
			if (item.getNodeName().equals("node")) {
				NamedNodeMap attributes = item.getAttributes();
				Node namedItemID = attributes.getNamedItem("id");
				String id = namedItemID.getNodeValue();
				Node namedItemLat = attributes.getNamedItem("lat");
				Node namedItemLon = attributes.getNamedItem("lon");
				String latitude = namedItemLat.getNodeValue();
				String longitude = namedItemLon.getNodeValue();
				osmNodes.add(new OSMNode(id, latitude, longitude));
			}
		}
		return osmNodes;
	}
	
	public static List<OSMLight> getLights(Document xmlDocument) {
		List<OSMLight> osmLights = new ArrayList<OSMLight>();
		Node osmRoot = xmlDocument.getFirstChild();
		NodeList osmXMLNodes = osmRoot.getChildNodes();
		List<OSMNode> nodesOfOneWay = new ArrayList<>();
		for (int i = 1; i < osmXMLNodes.getLength(); i++) {
			parseLightNode(osmXMLNodes.item(i), osmLights, nodesOfOneWay);
		}
		return osmLights;
	}
	
	private static void parseLightNode(Node item, List<OSMLight> osmLights, List<OSMNode> nodesOfOneWay) {
		String id, latitude, longitude, adherentWayId;
		if (item.getNodeName().equals("node")) {
			NamedNodeMap attributes = item.getAttributes();
			Node namedItemID = attributes.getNamedItem("id");
			Node namedItemLat = attributes.getNamedItem("lat");
			Node namedItemLon = attributes.getNamedItem("lon");
			id = namedItemID.getNodeValue();
			latitude = namedItemLat.getNodeValue();
			longitude = namedItemLon.getNodeValue();
			nodesOfOneWay.add(new OSMNode(id, latitude, longitude));
		} else if (item.getNodeName().equals("way")) {
			NamedNodeMap attributes = item.getAttributes();
			Node namedItemID = attributes.getNamedItem("id");
			adherentWayId = namedItemID.getNodeValue();
			addLightNodeSeries(osmLights, nodesOfOneWay, adherentWayId);
			nodesOfOneWay = new ArrayList<>();
		}
	}

	private static void addLightNodeSeries(List<OSMLight> osmLights, List<OSMNode> nodesOfOneWay,
			String adherentWayId) {
		for (OSMNode osmNode : nodesOfOneWay) {
			osmLights.add(new OSMLight(osmNode, adherentWayId));
		}
	}

	public static List<OSMStation> getStationNodes(Document xmlDocument) {
		List<OSMStation> stationNodes = new ArrayList<OSMStation>();
		Node osmRoot = xmlDocument.getFirstChild();
		NodeList osmXMLNodes = osmRoot.getChildNodes();
		for (int i = 1; i < osmXMLNodes.getLength(); i++) {
			Node item = osmXMLNodes.item(i);
			if (item.getNodeName().equals("node")) {
				NamedNodeMap attributes = item.getAttributes();
				Node namedItemID = attributes.getNamedItem("id");
				String id = namedItemID.getNodeValue();
				Node namedItemLat = attributes.getNamedItem("lat");
				Node namedItemLon = attributes.getNamedItem("lon");
				String latitude = namedItemLat.getNodeValue();
				String longitude = namedItemLon.getNodeValue();
				stationNodes.add(new OSMStation(id, latitude, longitude, "", null));
			}
		}
		return stationNodes;
	}
	
	public static List<RouteNode> getRouteNodes(Document xmlDocument) {
		List<RouteNode> routeNodes = new ArrayList<RouteNode>();
		Node osmRoot = xmlDocument.getFirstChild();
		NodeList osmXMLNodes = osmRoot.getChildNodes();
		for (int i = 1; i < osmXMLNodes.getLength(); i++) {
			Node item = osmXMLNodes.item(i);
			if (item.getNodeName().equals("node")) {
				NamedNodeMap attributes = item.getAttributes();
				Node namedItemID = attributes.getNamedItem("id");
				String id = namedItemID.getNodeValue();
				Node namedItemLat = attributes.getNamedItem("lat");
				Node namedItemLon = attributes.getNamedItem("lon");
				String latitude = namedItemLat.getNodeValue();
				String longitude = namedItemLon.getNodeValue();
				//routeNodes.add(new Station(id, latitude, longitude, "", null));
			}
		}
		return routeNodes;
	}
	   private static void parseBusInfo(Map<String, BrigadeInfo> brigadeNrToBrigadeInfo, OSMStation station, JSONObject jsonObject) {
	        JSONArray msg = (JSONArray) jsonObject.get("result");
	        Iterator iterator = msg.iterator();
	        while (iterator.hasNext()) {
	            JSONObject values =  (JSONObject) iterator.next();
	            JSONArray valuesArray = (JSONArray) values.get("values");
	            Iterator values_iterator = valuesArray.iterator();
	            String currentBrigadeNr = "";
	            while (values_iterator.hasNext()) {
	                JSONObject valueObject =  (JSONObject) values_iterator.next();
	                String key = (String)valueObject.get("key");
	                String value = (String)valueObject.get("value");
	                if (key.equals("brygada")) {
	                    currentBrigadeNr = value;
	                    if (!brigadeNrToBrigadeInfo.containsKey(value)) {
	                        brigadeNrToBrigadeInfo.put(value, new BrigadeInfo(value));
	                    }
	                }
	                else if (key.equals("czas")) {
	                     brigadeNrToBrigadeInfo.get(currentBrigadeNr).addToTimetable(station.getId(), value);
	                }
	            }
	        }
	    }
	/*private static void parseBusInfo(Map<String, BrigadeInfo> brigadeNrToBrigadeInfo, Station station, JSONObject jsonObject) {
        JSONArray msg = (JSONArray) jsonObject.get("result");
        Iterator<JSONArray> iterator = msg.iterator();
        while (iterator.hasNext()) {
        	
        	JSONObject values = iterator.next();
        	
        	Iterator<JSONObject> values_iterator = values.iterator();
        	String currentBrigadeNr = "";
        	
        	while (values_iterator.hasNext()) {
        		JSONObject valueObject =  (JSONObject)values_iterator.next();
        		String key = (String)valueObject.get("key");
        		String value = (String)valueObject.get("value");
        	    if (key.equals("brygada")) {
        	    	currentBrigadeNr = value;
        	    	if (!brigadeNrToBrigadeInfo.containsKey(value)) {
        	    		brigadeNrToBrigadeInfo.put(value, new BrigadeInfo(value));
        	    	}
        	    }
        	    else if (key.equals("czas")) {
        	    	 brigadeNrToBrigadeInfo.get(currentBrigadeNr).addToTimetable(station.getId(), value);
        	    }
        	}
        }
	}*/

	public static List<OSMNode> getOSMNodesInVicinity(double lat, double lon, double vicinityRange) throws IOException,
			SAXException, ParserConfigurationException {
		return MapAccessManager.getNodes(getNodesViaOverpass("<osm-script>\r\n" + 
				"  <query into=\"_\" type=\"node\">\r\n" + 
				"    <has-kv k=\"highway\" modv=\"\" v=\"traffic_signals\"/>\r\n" + 
				"    <bbox-query s=\"" + (lon - vicinityRange) + "\" w=\"" + (lat - vicinityRange) + "\" n=\"" + (lon + vicinityRange) + "\" e=\"" + (lat + vicinityRange) + "\"/>\r\n" + 
				"  </query>\r\n" + 
				"  <print e=\"\" from=\"_\" geometry=\"skeleton\" ids=\"yes\" limit=\"\" mode=\"body\" n=\"\" order=\"id\" s=\"\" w=\"\"/>\r\n" + 
				"</osm-script>"));
	}

	/**
	 * 
	 * @param query the overpass query
	 * @return the nodes in the formulated query
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public static Document getNodesViaOverpass(String query) throws IOException, ParserConfigurationException, SAXException {
		String hostname = OVERPASS_API;
		String queryString = query;//readFileAsString(query);

		URL osm = new URL(hostname);
		HttpURLConnection connection = (HttpURLConnection) osm.openConnection();
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

		DataOutputStream printout = new DataOutputStream(connection.getOutputStream());
		printout.writeBytes("data=" + URLEncoder.encode(queryString, "utf-8"));
		printout.flush();
		printout.close();

		DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
		return docBuilder.parse(connection.getInputStream());
	}
	
	public static JSONObject getNodesViaWarszawskie(String query) {
		URL nieOsm;
		Scanner scanner;
		String json = "";
		JSONParser parser = new JSONParser();
		JSONObject jObject= null;
		try {
			nieOsm = new URL(query);
			scanner = new Scanner(nieOsm.openStream());
			while (scanner.hasNext())
				json += scanner.nextLine();
			jObject = (JSONObject)parser.parse(json);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jObject;
	}

	public static List<OSMNode> sendHighwayOverpassQuery(PointList points) {
		List<OSMNode> nodes = new ArrayList<>();
		try {
			nodes = MapAccessManager.getNodes(getNodesViaOverpass("<osm-script>\r\n" + getHighwayQueries(points) + "</osm-script>"));
		} catch (Exception e) {
			e.printStackTrace();
		} finally { }
		return nodes;
	}

	private static List<OSMNode> sendLightAroundOverpassQuery(String lightAroundOverpassQuery) {
		List<OSMNode> nodes = new ArrayList<>();
		try {
			nodes = MapAccessManager.getNodes(getNodesViaOverpass(lightAroundOverpassQuery));
		} catch (Exception e) {
			e.printStackTrace();
		} finally { }
		return nodes;
	}
	
	public static List<RouteNode> sendFullWayAndItsTrafficSignalsQuery(List<Long> osmWayIds) {
		List<RouteNode> nodes = new ArrayList<>();
		try {
			nodes = MapAccessManager.getRouteNodes(getNodesViaOverpass(getFullWayAndItsTrafficSignalsQuery(osmWayIds)));
		} catch (Exception e) {
			e.printStackTrace();
		} finally { }
		return nodes;
	}
	
	public static List<OSMLight> sendFullTrafficSignalQuery(List<Long> osmWayIds) {
		List<OSMLight> nodes = new ArrayList<>();
		try {
			nodes = MapAccessManager.getLights(getNodesViaOverpass(getFullTrafficSignalQuery(osmWayIds)));
		} catch (Exception e) {
			e.printStackTrace();
		} finally { }
		return nodes;
	}
	
	public static List<OSMStation> sendStationOverpassQuery(String query) {
		List<OSMStation> nodes = new ArrayList<>();
		try {
			nodes = MapAccessManager.getStationNodes(getNodesViaOverpass(query));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return nodes;
	}
	
	private static String getHighwayQueries(PointList points) {
		StringBuilder builder = new StringBuilder();
		for (GHPoint point : points) {
			builder.append(getHighwayQuery(point));
		}
		return builder.toString();
	}
	
	private static String getHighwayQuery(GHPoint point) {
		return "<query into=\"_\" type=\"way\">\r\n" + 
				"<has-kv k=\"highway\" modv=\"\" regv=\"^(primary|secondary|secondary_link|tertiary|residential)$\"/>\r\n" + 
				"<around radius=\"5\" lat=\"" + point.lat + "\" lon=\"" + point.lon + "\"/>\r\n" + 
				"</query>\r\n" + 
				"<union into=\"_\">\r\n" + 
				"<item from=\"_\" into=\"_\"/>\r\n" + 
				"<recurse from=\"_\" into=\"_\" type=\"down\"/>\r\n" + 
				"</union>\r\n" + 
				"<print e=\"\" from=\"_\" geometry=\"skeleton\" ids=\"yes\" limit=\"\" mode=\"meta\" n=\"\" order=\"id\" s=\"\" w=\"\"/>\r\n";
	}
	
	private static String getFullWayAndItsTrafficSignalsQuery(List<Long> osmWayIds) {
		StringBuilder builder = new StringBuilder();
		builder.append("<osm-script>");
		for (long id : osmWayIds) {
			builder.append(getSingleWayAndItsTrafficSignalsQuery(id));
		}
		builder.append("</osm-script>");
		return builder.toString();
	}
	
	private static String getFullTrafficSignalQuery(List<Long> osmWayIds) {
		StringBuilder builder = new StringBuilder();
		builder.append("<osm-script>");
		for (long id : osmWayIds) {
			builder.append(getSingleTrafficSignalQuery(id));
		}
		builder.append("</osm-script>");
		return builder.toString();
	}
	
	private static String getSingleWayAndItsTrafficSignalsQuery(long osmWayId) {
		return "  <id-query type=\"way\" ref=\"" + osmWayId + "\" into=\"minor\"/>\r\n" + 
				"  <print e=\"\" from=\"minor\" geometry=\"skeleton\" ids=\"yes\" limit=\"\" mode=\"skeleton\" n=\"\" order=\"id\" s=\"\" w=\"\"/>\r\n" + 
				"  <query into=\"_\" type=\"node\">\r\n" + 
				"    <has-kv k=\"highway\" modv=\"\" v=\"traffic_signals\"/>\r\n" + 
				"    <recurse from=\"minor\" type=\"way-node\"/>\r\n" + 
				"  </query>\r\n" + 
				"  <print e=\"\" from=\"_\" geometry=\"skeleton\" ids=\"yes\" limit=\"\" mode=\"skeleton\" n=\"\" order=\"id\" s=\"\" w=\"\"/>\r\n";
	}
	
	private static String getSingleTrafficSignalQuery(long osmWayId) {
		return "<osm-script>\r\n" + 
				"  <id-query type=\"way\" ref=\"" + osmWayId + "\" into=\"minor\"/>\r\n" + 
				"  <query into=\"_\" type=\"node\">\r\n" + 
				"    <has-kv k=\"highway\" modv=\"\" v=\"traffic_signals\"/>\r\n" + 
				"    <recurse from=\"minor\" type=\"way-node\"/>\r\n" + 
				"  </query>\r\n" + 
				"  <print e=\"\" from=\"_\" geometry=\"skeleton\" ids=\"yes\" limit=\"\" mode=\"skeleton\" n=\"\" order=\"id\" s=\"\" w=\"\"/>\r\n" + 
				"  <id-query type=\"way\" ref=\"" + osmWayId + "\"/>\r\n" + 
				"  <print e=\"\" from=\"_\" geometry=\"skeleton\" ids=\"yes\" limit=\"\" mode=\"skeleton\" n=\"\" order=\"id\" s=\"\" w=\"\"/>\r\n" + 
				"</osm-script>";
	}
	
	/*private static String getSingleTrafficSignalQuery(long osmWayId) {
		return "  <id-query type=\"way\" ref=\"" + osmWayId + "\" into=\"minor\"/>\r\n" + 
				"  <query into=\"_\" type=\"node\">\r\n" + 
				"    <has-kv k=\"highway\" modv=\"\" v=\"traffic_signals\"/>\r\n" + 
				"    <recurse from=\"minor\" type=\"way-node\"/>\r\n" + 
				"  </query>\r\n" + 
				"  <print e=\"\" from=\"_\" geometry=\"skeleton\" ids=\"yes\" limit=\"\" mode=\"skeleton\" n=\"\" order=\"id\" s=\"\" w=\"\"/>\r\n";
	}*/

	/**
	 * 
	 * @param filePath
	 * @return
	 * @throws java.io.IOException
	 */
	private static String readFileAsString(String filePath) throws java.io.IOException {
		StringBuffer fileData = new StringBuffer(1000);
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}
		reader.close();
		return fileData.toString();
	}

	/**
	 * main method that simply reads some nodes
	 * 
	 * @param args
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public static List<OSMNode> traverseDatabase(double lat, double lon, double vicinityRange) throws IOException, SAXException, ParserConfigurationException {
		//Authenticator.setDefault(new java.net.CustomAuthenticator());
		List<OSMNode> osmNodesInVicinity = getOSMNodesInVicinity(lat, lon, vicinityRange);
		System.out.println("Traffic lights:");
		for (OSMNode osmNode : osmNodesInVicinity) {
			System.out.println(osmNode.getId() + ":" + osmNode.getLat() + ":" + osmNode.getLon());
		}
		return osmNodesInVicinity;
	}
	
	public static List<List<OSMNode>> getLights(PointList route) throws IOException, SAXException, ParserConfigurationException {
		List<List<OSMNode>> lights = new ArrayList<>();
		for (int i = 0; i < route.size() - 2; ++i) {
			//lights.add(getLightListAround(route.toGHPoint(i), route.toGHPoint(i + 2)));
		}
		return lights;
	}
	
	private static List<OSMNode> getLightListAround(GHPoint p1, GHPoint p2) throws IOException, SAXException, ParserConfigurationException {
		double midLat = (p1.lat - p2.lat) / 2.0;
		double midLon = (p1.lon - p2.lon) / 2.0;
		double vicinityRange = Math.max(p1.lat - p2.lat, p1.lon - p2.lon);
		return traverseDatabase(midLat, midLon, vicinityRange);
	}

	public static void prepareLightManagersInRadiusAndLightIdToLightManagerIdHashSet(SmartCityAgent smartCityAgent, GeoPosition middlePoint, int radius) {
		Document xmlDocument = getXmlDocument(CROSSROADS);
		Node osmRoot = xmlDocument.getFirstChild();
		NodeList districtXMLNodes = osmRoot.getChildNodes();
		for (int i = 0; i < districtXMLNodes.getLength(); i++) {
			if (districtXMLNodes.item(i).getNodeName().equals("district"))
				addAllDesiredIdsInDistrict(smartCityAgent, districtXMLNodes.item(i), middlePoint, radius);
		}
	}
	
	public static void prepareLightManagersInRadiusAndLightIdToLightManagerIdHashSetBeta(SmartCityAgent smartCityAgent,
			GeoPosition middlePoint, int radius) throws IOException, ParserConfigurationException, SAXException {
		List<OSMNode> lightsAround = sendLightsAroundOverpassQueryBeta(radius, middlePoint.getLatitude(), middlePoint.getLongitude());
		List<OSMNode> lightNodeList = sendParentWaysOfLightOverpassQueryBeta(lightsAround);
		List<OSMNode> lightsOfTypeA = filterByTypeA(lightNodeList);
		//fillChildNodesOfWays(lightsOfTypeA);
		prepareLightManagers(lightsOfTypeA);
	}

	private static List<OSMNode> sendLightsAroundOverpassQueryBeta(int radius, double middleLat, double middleLon) throws IOException, ParserConfigurationException, SAXException {
		List<OSMNode> lightNodes = getNodes(getNodesViaOverpass(getLightsAroundOverpassQueryBeta(radius, middleLat, middleLon)));
		return lightNodes;
	}
	
	private static String getLightsAroundOverpassQueryBeta(int radius, double lat, double lon) {
		return "<osm-script>\r\n" + 
				"  <query into=\"_\" type=\"node\">\r\n" + 
				"    <has-kv k=\"highway\" modv=\"\" v=\"traffic_signals\"/>\r\n" + 
				"    <around radius=\"" + radius + "\" lat=\"" + lat + "\" lon=\"" + lon + "\"/>\r\n" + 
				"  </query>\r\n" + 
				"  <print e=\"\" from=\"_\" geometry=\"skeleton\" ids=\"yes\" limit=\"\" mode=\"skeleton\" n=\"\" order=\"id\" s=\"\" w=\"\"/>\r\n" + 
				"</osm-script>";
	}

	private static List<OSMNode> sendParentWaysOfLightOverpassQueryBeta(final List<OSMNode> lightsAround)
			throws IOException, ParserConfigurationException, SAXException {
		final List<OSMNode> lightInfoList = parseLightNodeList(getNodesViaOverpass(getParentWaysOfLightOverpassQueryBeta(lightsAround)));
		return lightInfoList;
	}

	private static String getParentWaysOfLightOverpassQueryBeta(final List<OSMNode> lightsAround) {
		StringBuilder builder = new StringBuilder();
		builder.append("<osm-script>");
		for (final OSMNode light : lightsAround)
			builder.append(getSingleParentWaysOfLightOverpassQueryBeta(light.getId()));
		builder.append("</osm-script>");
		return builder.toString();
	}

	private static String getSingleParentWaysOfLightOverpassQueryBeta(final long osmLightId) {
		return "<id-query type=\"node\" ref=\"" + osmLightId + "\" into=\"crossroad\"/>\r\n" + 
				"  <union into=\"_\">\r\n" + 
				"    <item from=\"crossroad\" into=\"_\"/>\r\n" + 
				"    <recurse from=\"crossroad\" type=\"node-way\"/>\r\n" + 
				"  </union>\r\n" + 
				"  <print e=\"\" from=\"_\" geometry=\"full\" ids=\"yes\" limit=\"\" mode=\"body\" n=\"\" order=\"id\" s=\"\" w=\"\"/>";
	}

	private static List<OSMNode> parseLightNodeList(Document nodesViaOverpass) {
		List<OSMNode> lightNodeList = new ArrayList<>();
		Node osmRoot = nodesViaOverpass.getFirstChild();
		NodeList osmXMLNodes = osmRoot.getChildNodes();
		for (int i = 1; i < osmXMLNodes.getLength(); i++)
			parseLightNode(lightNodeList, osmXMLNodes.item(i));
		return lightNodeList;
	}

	private static void parseLightNode(List<OSMNode> lightNodeList, Node item) {
		if (item.getNodeName().equals("node")) {
			final OSMNode nodeWithParents = new OSMNode(item.getAttributes());
			lightNodeList.add(nodeWithParents);
		} else if (item.getNodeName().equals("way")) {
			final OSMWay osmWay = new OSMWay(item);
			lightNodeList.get(lightNodeList.size() - 1).addParentWay(osmWay);
		}
	}

	private static List<OSMNode> filterByTypeA(List<OSMNode> lightNodeList) {
		final List<OSMNode> typeAlightNodeList = new ArrayList<>();
		for (final OSMNode light : lightNodeList)
			if (light.isTypeA())
				typeAlightNodeList.add(light);
		return typeAlightNodeList;
	}

	private static void fillChildNodesOfWays(final List<OSMNode> lightsOfTypeA)
			throws IOException, ParserConfigurationException, SAXException {
		parseChildNodesOfWays(getNodesViaOverpass(getChildNodesOfWaysOverpassQueryBeta(lightsOfTypeA)), lightsOfTypeA); 
	}

	private static String getChildNodesOfWaysOverpassQueryBeta(final List<OSMNode> lightsOfTypeA) {
		StringBuilder builder = new StringBuilder();
		builder.append("<osm-script>");
		for (int i = 0; i < lightsOfTypeA.size(); ++i)
			builder.append(getSingleChildNodesOfWaysOverpassQueryBeta(lightsOfTypeA.get(i).getParentWayIds()));
		builder.append("</osm-script>");
		return builder.toString();
	}

	private static String getSingleChildNodesOfWaysOverpassQueryBeta(final List<Long> parentWayIds) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < parentWayIds.size(); ++i)
			builder.append(getSingleChildNodesOfSingleWayOverpassQueryBeta(parentWayIds.get(i)));
		builder.append(getSingleWayDelimiterOverpassQueryBeta());
		return builder.toString();
	}

	private static Object getSingleWayDelimiterOverpassQueryBeta() {
		return "<id-query type=\"way\" ref=\"" + DELIMITER_WAY + "\"/>\r\n" + 
				"  <print e=\"\" from=\"_\" geometry=\"skeleton\" ids=\"yes\" limit=\"\" mode=\"ids_only\" n=\"\" order=\"id\" s=\"\" w=\"\"/>\r\n";
	}

	private static String getSingleChildNodesOfSingleWayOverpassQueryBeta(final long osmWayId) {
		return "<id-query type=\"way\" ref=\"" + osmWayId + "\" into=\"crossroadEntrance\"/>\r\n" + 
				"  <union into=\"_\">\r\n" + 
				"    <recurse from=\"crossroadEntrance\" type=\"way-node\"/>\r\n" + 
				"    <id-query type=\"relation\" ref=\"" + DELIMITER_RELATION + "\"/>\r\n" + 
				"  </union>\r\n" + 
				"  <print e=\"\" from=\"_\" geometry=\"skeleton\" ids=\"yes\" limit=\"\" mode=\"ids_only\" n=\"\" order=\"id\" s=\"\" w=\"\"/>";
	}

	private static void parseChildNodesOfWays(Document childNodesOfWays, List<OSMNode> lightsOfTypeA) {
		Node osmRoot = childNodesOfWays.getFirstChild();
		NodeList osmXMLNodes = osmRoot.getChildNodes();
		int lightIndex = 0, parentWayIndex = 0;
		for (int i = 1; i < osmXMLNodes.getLength(); i++) {
			final Node item = osmXMLNodes.item(i);
			if (item.getNodeName().equals("node")) {
				lightsOfTypeA.get(lightIndex).addChildNodeIdForParentWay(parentWayIndex,
						item.getAttributes().getNamedItem("id").getNodeValue());
			} else if (item.getNodeName().equals("relation")) {
				++parentWayIndex;
			} else if (item.getNodeName().equals("way")) {
				++lightIndex;
				parentWayIndex = 0;
			}
		}
	}

	private static void prepareLightManagers(List<OSMNode> lightsOfTypeA) {
		for (final OSMNode centerCrossroadNode : lightsOfTypeA) {
			if (centerCrossroadNode.determineParentOrientationsTowardsCrossroad())
				SmartCityAgent.tryAddNewLightManagerAgent(centerCrossroadNode);
		}
	}
	
	private static void addAllDesiredIdsInDistrict(SmartCityAgent smartCityAgent, Node districtRoot, GeoPosition middlePoint, int radius) {
		Node crossroadsRoot = districtRoot.getChildNodes().item(1);
		NodeList crossroadXMLNodes = crossroadsRoot.getChildNodes();
		for (int i = 0; i < crossroadXMLNodes.getLength(); ++i) {
			if (crossroadXMLNodes.item(i).getNodeName().equals("crossroad"))
				addCrossroadIdIfDesired(smartCityAgent, crossroadXMLNodes.item(i), middlePoint, radius);
		}
	}
	
	private static void addCrossroadIdIfDesired(SmartCityAgent smartCityAgent, Node crossroad, GeoPosition middlePoint, int radius) {
		Pair<Double, Double> crossroadLatLon = calculateLatLonBasedOnInternalLights(crossroad);
		
		if (belongsToCircle(crossroadLatLon.getValue0(), crossroadLatLon.getValue1(), middlePoint, radius)) {
			SmartCityAgent.tryAddNewLightManagerAgent(crossroad);
		}
	}
	
	private static Pair<Double, Double> calculateLatLonBasedOnInternalLights(Node crossroad) {
		List<Double> latList = getParametersFromGroup(getCrossroadGroup(crossroad, 1),
				getCrossroadGroup(crossroad, 3), LAT);
		List<Double> lonList = getParametersFromGroup(getCrossroadGroup(crossroad, 1),
				getCrossroadGroup(crossroad, 3), LON);
		double latAverage = calculateAverage(latList);
		double lonAverage = calculateAverage(lonList);
		return Pair.with(latAverage, lonAverage);
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
			if (lightNodes.item(i).getNodeName().equals("light"))
				list.add(Double.parseDouble(lightNodes.item(i).getAttributes().getNamedItem(parameterName).getNodeValue()));
		}
	}
	
	private static double calculateAverage(List<Double> doubleList) {
		double sum = 0;
		for (double value : doubleList) {
			sum += value;
		}
		return sum / (double)(doubleList.size());
	}
	
	public static Node getCrossroadGroup(Node crossroad, int index) {
		return crossroad.getChildNodes().item(index);
	}

	public static boolean belongsToCircle(double latToBelong, double lonToBelong, GeoPosition middlePoint, int radius) {
		return (((latToBelong - middlePoint.getLatitude()) * (latToBelong - middlePoint.getLatitude()))
				+ ((lonToBelong - middlePoint.getLongitude()) * (lonToBelong - middlePoint.getLongitude())))
				< (radius * radius)*0.0000089*0.0000089;
	}
	
	private static Document getXmlDocument(String filepath) {
		DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
		Document document = null;
		try {
			DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
			document = docBuilder.parse(new File(filepath));
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} finally { }
		return document;
	}

	public static Set<OSMStation> getStations(GeoPosition middlePoint, int radius) {
		List<OSMStation> stationNodes = sendStationOverpassQuery(getStationsInRadiusQuery(middlePoint, radius));
		return new LinkedHashSet<>(stationNodes);
	}

	private static String getStationsInRadiusQuery(GeoPosition middlePoint, int radius) {
		return "<osm-script>\r\n" + 
				"  <query into=\"_\" type=\"area\">\r\n" + 
				"    <has-kv k=\"name\" modv=\"\" v=\"Warszawa\"/>\r\n" + 
				"  </query>\r\n" + 
				"  <query into=\"_\" type=\"node\">\r\n" + 
				"    <has-kv k=\"highway\" modv=\"\" v=\"bus_stop\"/>\r\n" + 
				"    <around radius=\"" + radius + "\" lat=\"" + middlePoint.getLatitude() + "\" lon=\"" + middlePoint.getLongitude() + "\"/>\r\n" + 
				"  </query>\r\n" + 
				"  <print e=\"\" from=\"_\" geometry=\"skeleton\" ids=\"yes\" limit=\"\" mode=\"body\" n=\"\" order=\"id\" s=\"\" w=\"\"/>\r\n" + 
				"</osm-script>";
	}

	private static String getLightAroundOverpassQuery(GeoPosition middlePoint, int radius) {
		return "<osm-script>\n" + 
				"  <query into=\"_\" type=\"area\">\n" + 
				"    <has-kv k=\"name\" modv=\"\" v=\"Warszawa\"/>\n" + 
				"  </query>\n" + 
				"  <query into=\"_\" type=\"node\">\n" + 
				"    <has-kv k=\"highway\" modv=\"\" v=\"traffic_signals\"/>\n" + 
				"    <around radius=\"" + radius + "\" lat=\"" + middlePoint.getLatitude() + "\" lon=\"" + middlePoint.getLongitude() + "\"/>\n" + 
				"  </query>\n" + 
				"  <print e=\"\" from=\"_\" geometry=\"skeleton\" ids=\"yes\" limit=\"\" mode=\"body\" n=\"\" order=\"id\" s=\"\" w=\"\"/>\n" + 
				"</osm-script>";
	}

	public static Set<BusInfo> getBusInfo(int radius, double middleLat, double middleLon) {
		System.out.println("STEP 2/" + SmartCityAgent.STEPS + ": Sending bus overpass query");
		Set<BusInfo> infoSet = sendBusOverpassQuery(radius, middleLat, middleLon);
		System.out.println("STEP 4/" + SmartCityAgent.STEPS + ": Starting warzawskie query and parsing");
		int i = 0;
		for (BusInfo info : infoSet) {
			System.out.println("STEP 4/" + SmartCityAgent.STEPS + " (SUBSTEP " + (++i) + "/" + infoSet.size() + "): Warszawskie query sending & parsing substep");
			sendBusWarszawskieQuery(info);
		}
		return infoSet;
	}

	private static Set<BusInfo> sendBusOverpassQuery(int radius, double middleLat, double middleLon) {
		Set<BusInfo> infoSet = null;
		try {
			infoSet = MapAccessManager.parseBusInfo(getNodesViaOverpass(getBusOverpassQuery(radius, middleLat, middleLon)), radius, middleLat, middleLon);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return infoSet;
	}

	private static Set<BusInfo> parseBusInfo(Document nodesViaOverpass, int radius, double middleLat, double middleLon) {
		System.out.println("STEP 3/" + SmartCityAgent.STEPS + ": Starting overpass bus info parsing");
		Set<BusInfo> infoSet = new LinkedHashSet<>();
		Node osmRoot = nodesViaOverpass.getFirstChild();
		NodeList osmXMLNodes = osmRoot.getChildNodes();
		for (int i = 1; i < osmXMLNodes.getLength(); i++) {
			Node item = osmXMLNodes.item(i);
			if (item.getNodeName().equals("relation")) {
				BusInfo info = new BusInfo();
				infoSet.add(info);
				NodeList member_list=item.getChildNodes();
				StringBuilder builder = new StringBuilder();
				for (int j=0;j<member_list.getLength();j++) {
					Node member = member_list.item(j);
					if (member.getNodeName().equals("member") ) {
						NamedNodeMap attributes = member.getAttributes();
						Node namedItemID = attributes.getNamedItem("ref");
						if (attributes.getNamedItem("role").getNodeValue().contains("stop") && attributes.getNamedItem("type").getNodeValue().equals("node")) {
							info.addStation(namedItemID.getNodeValue());
						} else if (attributes.getNamedItem("role").getNodeValue().length() == 0 && attributes.getNamedItem("type").getNodeValue().equals("way")) {
							appendSingleBusWayOverpassQuery(builder, Long.parseLong(namedItemID.getNodeValue()));
						}
					}
					else if (member.getNodeName().equals("tag")) {
						NamedNodeMap attributes = member.getAttributes();
						Node namedItemID = attributes.getNamedItem("k");
					    if (namedItemID.getNodeValue().equals("ref")) {
					    	Node number_of_line=attributes.getNamedItem("v");
					    	info.setBusLine(number_of_line.getNodeValue());
					    }
					}
				}
		        try {
		        	info.setRoute(MapAccessManager.parseOsmWay(getNodesViaOverpass(getBusWayOverpassQueryWithPayload(builder)), radius, middleLat, middleLon));
		        	
		        } catch (Exception e) {
		        	e.printStackTrace();
		        }
			}
			if (item.getNodeName().equals("node")) {
				NamedNodeMap attributes = item.getAttributes();
				
				String osmId = attributes.getNamedItem("id").getNodeValue();
				String lat = attributes.getNamedItem("lat").getNodeValue();
				String lon = attributes.getNamedItem("lon").getNodeValue();
				
				if (!SmartCityAgent.osmIdToStationOSMNode.containsKey(Long.parseLong(osmId))) {
					NodeList list_tags = item.getChildNodes();
					for (int z=0; z<list_tags.getLength();z++) {
						Node tag = list_tags.item(z);
						if (tag.getNodeName().equals("tag") ) {
							NamedNodeMap attr = tag.getAttributes();
							Node kAttr= attr.getNamedItem("k");
							if (kAttr.getNodeValue().equals("public_transport")) {
								Node vAttr = attr.getNamedItem("v");
								if (!vAttr.getNodeValue().contains("stop"))
									break;
							} else if (kAttr.getNodeValue().equals("ref")) {
						    	Node number_of_station=attr.getNamedItem("v");
						    	OSMStation stationOSMNode = new OSMStation(osmId, lat, lon, number_of_station.getNodeValue());
						    	SmartCityAgent.tryAddNewStationAgent(stationOSMNode);
						    }
						}
					}
				}
			}
		}
		
		for (BusInfo info : infoSet) {
			info.filterStationsByCircle(middleLat, middleLon, radius);
		}
		
		return infoSet;
	}

	private static List<OSMWay> parseOsmWay(Document nodesViaOverpass, int radius, double middleLat, double middleLon) {
		  List<OSMWay> route = new ArrayList<>();
		  Node osmRoot = nodesViaOverpass.getFirstChild();
			NodeList osmXMLNodes = osmRoot.getChildNodes();
			for (int i = 1; i < osmXMLNodes.getLength(); i++) 
			{
				Node item = osmXMLNodes.item(i);
				if (item.getNodeName().equals("way")) 
				{
					OSMWay way = new OSMWay(item);
					if (way.startsInCircle(radius, middleLat, middleLon)) // TODO CORRECT POTENTIAL BUGS CAUSING ROUTE TO BE CUT INTO PIECES BECAUSE OF RZĄŻEWSKI CASE
						route.add(way);
				}
		    }
		return route;
	}

	private static void appendSingleBusWayOverpassQuery(StringBuilder query, long osmWayId) {
	        query.append("<id-query type=\"way\" ref=\"" + osmWayId + "\"/>\r\n" +
	                "  <print e=\"\" from=\"_\" geometry=\"full\" ids=\"yes\" limit=\"\" mode=\"skeleton\" n=\"\" order=\"id\" s=\"\" w=\"\"/>");
	    }
	   
	    private static String getBusWayOverpassQueryWithPayload(StringBuilder query) {
	        return "<osm-script>\r\n" +
	                query.toString() +
	                "</osm-script>";
	    }
	private static String getBusOverpassQuery(int radius, double middleLat, double middleLon) {
		return "<osm-script>\r\n" + 
				"  <query into=\"_\" type=\"relation\">\r\n" + 
				"    <has-kv k=\"route\" modv=\"\" v=\"bus\"/>\r\n" + 
				"    <around radius=\""+radius+"\" lat=\""+middleLat+"\" lon=\""+middleLon+"\"/>\r\n" + 
				"  </query>\r\n" + 
				"  <print e=\"\" from=\"_\" geometry=\"skeleton\" ids=\"yes\" limit=\"\" mode=\"body\" n=\"\" order=\"id\" s=\"\" w=\"\"/>\r\n" + 
				"  <recurse type=\"relation-node\"/>\r\n" + 
				"  <print e=\"\" from=\"_\" geometry=\"skeleton\" ids=\"yes\" limit=\"\" mode=\"body\" n=\"\" order=\"id\" s=\"\" w=\"\"/>\r\n" + 
				"</osm-script>";
	}

	private static void sendBusWarszawskieQuery(BusInfo info) {
		Map<String, BrigadeInfo> brigadeNrToBrigadeInfo = new HashMap<>();
		for (OSMStation station : info.getStations()) {
			try {
				MapAccessManager.parseBusInfo(brigadeNrToBrigadeInfo, station, getNodesViaWarszawskie(getBusWarszawskieQuery(station.getBusStopId(), station.getBusStopNr(), info.getBusLine())));
			} catch (NullPointerException gowno) {
				gowno.printStackTrace();
			}
		}
		info.setBrigadeList(brigadeNrToBrigadeInfo.values());
	}

	private static String getBusWarszawskieQuery(String busStopId, String busStopNr, String busLine) {
		return "https://api.um.warszawa.pl/api/action/dbtimetable_get/?id=e923fa0e-d96c-43f9-ae6e-60518c9f3238&busstopId=" + busStopId + "&busstopNr=" + busStopNr + "&line=" + busLine + "&apikey=400dacf8-9cc4-4d6c-82cc-88d9311401a5";
	}
}
