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
package SmartCity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.graphhopper.util.shapes.GHPoint;

import Agents.LightManager;
import GUI.OSMNode;

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

	private static final String OVERPASS_API = "http://www.overpass-api.de/api/interpreter";
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
				osmNodes.add(new OSMNode(id, latitude, longitude, "", null));
			}
		}
		return osmNodes;
	}
	
	public static List<Station> getStationNodes(Document xmlDocument) {
		List<Station> stationNodes = new ArrayList<Station>();
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
				stationNodes.add(new Station(id, latitude, longitude, "", null));
			}
		}
		return stationNodes;
	}

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

	public static List<OSMNode> sendHighwayOverpassQuery(PointList points) {
		List<OSMNode> nodes = null;
		try {
			nodes = MapAccessManager.getNodes(getNodesViaOverpass("<osm-script>\r\n" + getHighwayQueries(points) + "</osm-script>"));
		} catch (Exception e) {
			e.printStackTrace();
		} finally { }
		return nodes;
	}

	private static List<OSMNode> sendLightAroundOverpassQuery(String lightAroundOverpassQuery) {
		List<OSMNode> nodes = null;
		try {
			nodes = MapAccessManager.getNodes(getNodesViaOverpass(lightAroundOverpassQuery));
		} catch (Exception e) {
			e.printStackTrace();
		} finally { }
		return nodes;
	}
	
	public static List<OSMNode> sendTrafficSignalOverpassQuery(List<Long> osmWayIds) {
		List<OSMNode> nodes = null;
		try {
			nodes = MapAccessManager.getNodes(getNodesViaOverpass(getFullTrafficSignalQuery(osmWayIds)));
		} catch (Exception e) {
			e.printStackTrace();
		} finally { }
		return nodes;
	}
	
	public static List<Station> sendStationOverpassQuery(String query) {
		List<Station> nodes = null;
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
	
	private static String getFullTrafficSignalQuery(List<Long> osmWayIds) {
		StringBuilder builder = new StringBuilder();
		builder.append("<osm-script>");
		for (long id : osmWayIds) {
			builder.append(getSingleTrafficSignalQuery(id));
		}
		builder.append("</osm-script>");
		return builder.toString();
	}
	
	private static String getSingleTrafficSignalQuery(long osmWayId) {
		return "<id-query type=\"way\" ref=\"" + osmWayId + "\" into=\"minor\"/>\r\n" + 
				"  <query into=\"_\" type=\"node\">\r\n" + 
				"    <has-kv k=\"highway\" modv=\"\" v=\"traffic_signals\"/>\r\n" + 
				"    <recurse from=\"minor\" type=\"way-node\"/>\r\n" + 
				"  </query>\r\n" + 
				"  <print e=\"\" from=\"_\" geometry=\"skeleton\" ids=\"yes\" limit=\"\" mode=\"body\" n=\"\" order=\"id\" s=\"\" w=\"\"/>";
	}

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
			smartCityAgent.tryAddNewLightManagerAgent(crossroad);
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

	private static boolean belongsToCircle(double latToBelong, double lonToBelong, GeoPosition middlePoint, int radius) {
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

	public static Set<Station> getStations(GeoPosition middlePoint, int radius) {
		List<Station> stationNodes = sendStationOverpassQuery(getStationsInRadiusQuery(middlePoint, radius));
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
}