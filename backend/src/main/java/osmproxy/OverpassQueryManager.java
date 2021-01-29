package osmproxy;

import routing.core.IGeoPosition;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class OverpassQueryManager {
    static String getFullTrafficSignalQuery(List<Long> osmWayIds) {
        var builder = new StringBuilder();
        builder.append("<osm-script>");
        for (long id : osmWayIds) {
            builder.append(getSingleTrafficSignalQuery(id));
        }
        builder.append("</osm-script>");
        return builder.toString();
    }

    static String getMultipleWayAndItsNodesQuery(List<Long> osmWayIds) {
        var builder = new StringBuilder();
        builder.append("<osm-script>");
        for (long id : osmWayIds) {
            builder.append(getSingleWayAndItsNodesQuery(id));
        }
        builder.append("</osm-script>");
        return builder.toString();
    }

	static List<String> getMultipleWayAndItsNodesQuerySplit(List<Long> osmWayIds) {
		int partitionSize = 1000;
		List<List<Long>> partitions = new LinkedList<List<Long>>();
		for (int i = 0; i < osmWayIds.size(); i += partitionSize) {
		    partitions.add(osmWayIds.subList(i,
		            Math.min(i + partitionSize, osmWayIds.size())));
		}
		List<String> queries = new ArrayList<>();
		for (var part : partitions) {
			queries.add(getMultipleWayAndItsNodesQuery(part));
		}
		return queries;
	}

    static String getWaysQuery(double lat, double lon, int radius) {
    	return "<osm-script>\r\n"
    			+ "  <query into=\"_\" type=\"way\">\r\n"
    			+ "    <around radius=\"" + radius + "\" lat=\"" + lat + "\" lon=\"" + lon + "\"/>\r\n"
    			+ "  </query>\r\n"
    			+ "  <print e=\"\" from=\"_\" geometry=\"skeleton\" ids=\"yes\" limit=\"\" mode=\"ids_only\" n=\"\" order=\"id\" s=\"\" w=\"\"/>\r\n"
    			+ "</osm-script>";
    }

    private static String getSingleWayAndItsNodesQuery(long osmWayId) {
        return "<id-query type=\"way\" ref=\"" + osmWayId + "\" into=\"minor\"/>\r\n" +
                "  <item from=\"minor\" into=\"_\"/>\r\n" +
                "  <print e=\"\" from=\"_\" geometry=\"full\" ids=\"yes\" limit=\"\" mode=\"body\" n=\"\" order=\"id\" s=\"\" w=\"\"/>\r\n" +
                "  <recurse from=\"minor\" type=\"way-node\"/>\r\n" +
                "  <print e=\"\" from=\"_\" geometry=\"skeleton\" ids=\"yes\" limit=\"\" mode=\"tags\" n=\"\" order=\"id\" s=\"\" w=\"\"/>\r\n";
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


    public static String getSingleBusWayQuery(long osmWayId) {
        return "<id-query type=\"way\" ref=\"" + osmWayId + "\"/>\r\n" +
                "  <print e=\"\" from=\"_\" geometry=\"full\" ids=\"yes\" limit=\"\" mode=\"skeleton\" n=\"\" order=\"id\" s=\"\" w=\"\"/>";
    }

    public static String getQueryWithPayload(String query) {
        return "<osm-script>\r\n" +
                query +
                "</osm-script>";
    }

    public static String getBusQuery(IGeoPosition pos, int radius) {
        return getBusQuery(pos.getLat(), pos.getLng(), radius);
    }

    private static String getBusQuery(double middleLat, double middleLon, int radius) {
        return "<osm-script>\r\n" +
                "  <query into=\"_\" type=\"relation\">\r\n" +
                "    <has-kv k=\"route\" modv=\"\" v=\"bus\"/>\r\n" +
                "    <around radius=\"" + radius + "\" lat=\"" + middleLat + "\" lon=\"" + middleLon + "\"/>\r\n" +
                "  </query>\r\n" +
                "  <print e=\"\" from=\"_\" geometry=\"skeleton\" ids=\"yes\" limit=\"\" mode=\"body\" n=\"\" order=\"id\" s=\"\" w=\"\"/>\r\n" +
                "  <recurse type=\"relation-node\"/>\r\n" +
                "  <print e=\"\" from=\"_\" geometry=\"skeleton\" ids=\"yes\" limit=\"\" mode=\"body\" n=\"\" order=\"id\" s=\"\" w=\"\"/>\r\n" +
                "</osm-script>";
    }

    static String getLightsAroundQuery(IGeoPosition pos, int radius) {
        return getLightsAroundQuery(pos.getLat(), pos.getLng(), radius);
    }

    private static String getLightsAroundQuery(double lat, double lon, int radius) {
        return "<osm-script>\r\n" +
                "  <query into=\"_\" type=\"node\">\r\n" +
                "    <has-kv k=\"highway\" modv=\"\" v=\"traffic_signals\"/>\r\n" +
                "    <around radius=\"" + radius + "\" lat=\"" + lat + "\" lon=\"" + lon + "\"/>\r\n" +
                "  </query>\r\n" +
                "  <print e=\"\" from=\"_\" geometry=\"skeleton\" ids=\"yes\" limit=\"\" mode=\"skeleton\" n=\"\" order=\"id\" s=\"\" w=\"\"/>\r\n" +
                "</osm-script>";
    }

    static String getSingleParentWaysOfLightQuery(final long osmLightId) {
        return "<id-query type=\"node\" ref=\"" + osmLightId + "\" into=\"crossroad\"/>\r\n" +
                "  <union into=\"_\">\r\n" +
                "    <item from=\"crossroad\" into=\"_\"/>\r\n" +
                "    <recurse from=\"crossroad\" type=\"node-way\"/>\r\n" +
                "  </union>\r\n" +
                "  <print e=\"\" from=\"_\" geometry=\"full\" ids=\"yes\" limit=\"\" mode=\"body\" n=\"\" order=\"id\" s=\"\" w=\"\"/>";
    }
}
