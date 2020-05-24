package SmartCity;

import java.util.ArrayList;
import java.util.List;

import org.jxmapviewer.viewer.GeoPosition;

public class OSMWay {

	List<GeoPosition> coordinates = new ArrayList<>();
     Long ID ;
	public OSMWay(String nodeValue) {
		ID=Long.parseLong(nodeValue);
	}

	public void addPoint(double lat, double lng) {
		coordinates.add(new GeoPosition(lat, lng));
		
	}

	public long getOsmWayId() {
		return ID;
	}
	public List<GeoPosition> getCoordinates()
		{
		return coordinates;
		}
}
