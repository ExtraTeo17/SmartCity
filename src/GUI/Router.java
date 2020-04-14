package GUI;

import SmartCity.RoutePainter;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopperAPI;
import com.graphhopper.PathWrapper;
import com.graphhopper.Trip.Leg;
import com.graphhopper.api.GraphHopperGeocoding;
import com.graphhopper.api.GraphHopperMatrixWeb;
import com.graphhopper.api.GraphHopperWeb;
import com.graphhopper.jackson.GraphHopperModule;
import com.graphhopper.util.PointList;
import com.graphhopper.util.details.PathDetail;
import com.graphhopper.util.shapes.GHPoint;
import com.graphhopper.util.shapes.GHPoint3D;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class Router {
    private HighwayAccessor accessor;
	private GeoPosition pointA = null;
	private GeoPosition pointB = null;
	
	public Router() {
		HighwayAccessor accessor = new HighwayAccessor();
	}
	
	public void addPoint(JXMapViewer viewer, GeoPosition position) {
		if (pointA == null) {
			pointA = position;
		} else {
			pointB = position;
			drawRoute(viewer, getRoute(pointA, pointB));
			pointA = null;
			pointB = null;
		}
	}
	
	private PointList getRoute(GeoPosition pointA, GeoPosition pointB) {
		GHRequest req = new GHRequest(pointA.getLatitude(), pointA.getLongitude(),
				pointB.getLatitude(), pointB.getLongitude()).
			    setWeighting("fastest").
			    setVehicle("car").
			    setLocale(Locale.US);
		GraphHopperWeb ghweb = new GraphHopperWeb().setKey("cdd7f8b1-921e-4b98-bed2-f8f22ce919e5");
		GHResponse rsp = ghweb.route(req);
		if (rsp.hasErrors()) {
			System.out.println(rsp.getErrors());
			return null;
		}
		PathWrapper path = rsp.getBest();
		System.out.println(path.getPoints());
		return path.getPoints();
	}
	
	private GHPoint geoPosToGhPoint(GeoPosition position) {
		return new GHPoint(position.getLatitude(), position.getLongitude());
	}
	
	private void drawRoute(JXMapViewer viewer, PointList route) {
		List<GeoPosition> track = new ArrayList<>();
		for (GHPoint3D point : route) {
			track.add(new GeoPosition(point.lat, point.lon));
		}
		System.out.println(accessor.getHighways(route));
		drawTrack(viewer, track);
	}
	
	private void drawTrack(JXMapViewer viewer, List<GeoPosition> track) {
        RoutePainter routePainter = new RoutePainter(track);
        WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<Waypoint>();
        waypointPainter.setWaypoints(getWaypoints(track));
        
        // Create a compound painter that uses both the route-painter and the waypoint-painter
        List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();
        painters.add(routePainter);
        painters.add(waypointPainter);
        
        CompoundPainter<JXMapViewer> painter = new CompoundPainter<JXMapViewer>(painters);
        viewer.setOverlayPainter(painter);
	}
	
	private Set<Waypoint> getWaypoints(List<GeoPosition> track) {
		Set<Waypoint> set = new HashSet<>();
		for (GeoPosition pos : track) {
			set.add(new DefaultWaypoint(pos.getLatitude(), pos.getLongitude()));
		}
		return set;
	}
}
