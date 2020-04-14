package GUI;

import java.util.Map;

public class OSMNode {
	
	public OSMNode(String id2, String latitude, String longitude, String version2, Map<String, String> tags2) {
		id = id2;
		lat = latitude;
		lon = longitude;
		version = version2;
		tags = tags2;
	}

	private String id;
	
	private String lat;
	
	private String lon;
	
	private final Map<String, String> tags;

	private String version;

	public String getId() {
		return id;
	}
	
	public String getLat() {
		return lat;
	}
	
	public String getLon() {
		return lon;
	}
}