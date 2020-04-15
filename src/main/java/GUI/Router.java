package GUI;

import SmartCity.MapAccessManager;
import SmartCity.RoutePainter;

import org.javatuples.Pair;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopperAPI;
import com.graphhopper.PathWrapper;
import com.graphhopper.osmidexample.HighwayAccessor;
import com.graphhopper.util.CmdArgs;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.Parameters;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import com.graphhopper.util.shapes.GHPoint3D;
import com.graphhopper.routing.AStar;
import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.Dijkstra;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.QueryGraph;
import com.graphhopper.routing.RoutingAlgorithm;
import com.graphhopper.routing.VirtualEdgeIteratorState;
import com.graphhopper.routing.ch.PrepareContractionHierarchies;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.FlagEncoderFactory;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.storage.Directory;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphBuilder;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.storage.RAMDirectory;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.LocationIndexTree;
import com.graphhopper.storage.index.QueryResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class Router {
    private HighwayAccessor2 accessor;
	private GeoPosition pointA = null;
	private GeoPosition pointB = null;
	
	public Router() {
		HighwayAccessor2 accessor = new HighwayAccessor2();
	}
	
	public void addPoint(JXMapViewer viewer, GeoPosition position) {
		if (pointA == null) {
			pointA = position;
		} else {
			pointB = position;
			drawRouteAndLights(viewer);
			pointA = null;
			pointB = null;
		}
	}
	
	private void drawRouteAndLights(JXMapViewer viewer) {
		Pair<List<Long>, PointList> osmWayIdsAndPointList = getRoute(pointA, pointB);
		List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();
		drawRoute(painters, osmWayIdsAndPointList.getValue1());
		drawLights(painters, MapAccessManager.sendTrafficSignalOverpassQuery(osmWayIdsAndPointList.getValue0()));
        CompoundPainter<JXMapViewer> painter = new CompoundPainter<JXMapViewer>(painters);
        viewer.setOverlayPainter(painter);
	}

	private Pair<List<Long>, PointList> getRoute(GeoPosition pointA, GeoPosition pointB) {
		/*GHRequest req = new GHRequest(pointA.getLatitude(), pointA.getLongitude(),
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
		System.out.println(path.getPoints());*/
		
		Pair<List<Long>, PointList> osmWayIdsAndPointList = HighwayAccessor.getOsmWayIdsAndPointList(new String[] {"config=config.properties", "datareader.file=mazowieckie-latest.osm.pbf"}, pointA.getLatitude(), pointA.getLongitude(), pointB.getLatitude(), pointB.getLongitude());
		
		return osmWayIdsAndPointList;
	}
	
	private void testFunction() {
		mainLol(new String[] { "config=config.properties" });
	}
	
	private void lol2(GeoPosition pointA, GeoPosition pointB) {
		/*FlagEncoder encoder = new CarFlagEncoder();
		EncodingManager em = EncodingManager.create(encoder);
		GraphHopperStorage gb = new GraphBuilder(em).setLocation("graphhopper_folder").setStore(true).build();
		// Load index
		LocationIndex index = new LocationIndexTree(gb.getBaseGraph(), new RAMDirectory("graphhopper_folder", true));
		if (!index.loadExisting())
		    throw new IllegalStateException("location index cannot be loaded!");
		
		QueryResult fromQR = index.findClosest(pointA.getLatitude(), pointA.getLongitude(), EdgeFilter.ALL_EDGES);
		QueryResult toQR = index.findClosest(pointB.getLatitude(), pointB.getLongitude(), EdgeFilter.ALL_EDGES);
		QueryGraph queryGraph = new QueryGraph(queryGraph);
		queryGraph.lookup(fromQR, toQR);
		AlgorithmOptions algoOpts = AlgorithmOptions.start().
				   algorithm(Parameters.Algorithms.DIJKSTRA_BI).traversalMode(TraversalMode.NODE_BASED).weighting(new FastestWeighting(encoder)).
				   build();
		RoutingAlgorithm algorithm = pch.createAlgo(queryGraph, algoOpts);
		Path path = algorithm.calcPath(fromQR.getClosestNode(), toQR.getClosestNode());*/
	}
	
	private Path lol3(GeoPosition pointA, GeoPosition pointB) {
		FlagEncoder encoder = new CarFlagEncoder();
		EncodingManager em = EncodingManager.create(FlagEncoderFactory.DEFAULT, "graphhopper_folder");
		Weighting weighting = new FastestWeighting(encoder);
		// Creating and saving the graph
		GraphBuilder gb = new GraphBuilder(em).
		    setLocation("graphhopper_folder").
		    setStore(true).
		    setCHGraph(weighting);
		GraphHopperStorage hopperStorage = gb.create();
		CHGraph graph = gb.chGraphCreate(weighting);
		Directory dir = new RAMDirectory("graphhopper_folder", true);

		// Prepare the graph for fast querying ...
		TraversalMode tMode = TraversalMode.NODE_BASED;
		PrepareContractionHierarchies pch = new PrepareContractionHierarchies(dir, hopperStorage, graph, weighting, tMode);
		pch.doWork();

		// flush after preparation!
		hopperStorage.flush();

		// Load and use the graph
		GraphStorage storage = gb.load();

		// Load index
		LocationIndex index = new LocationIndexTree(hopperStorage.getBaseGraph(), dir);
		if (!index.loadExisting())
		    throw new IllegalStateException("location index cannot be loaded!");

		// calculate path is identical
		QueryResult fromQR = index.findClosest(pointA.getLatitude(), pointA.getLongitude(), EdgeFilter.ALL_EDGES);
		QueryResult toQR = index.findClosest(pointB.getLatitude(), pointB.getLongitude(), EdgeFilter.ALL_EDGES);
	    QueryGraph queryGraph = new QueryGraph(graph);
		queryGraph.lookup(fromQR, toQR);

		// create the algorithm using the PrepareContractionHierarchies object
		AlgorithmOptions algoOpts = AlgorithmOptions.start().
		   algorithm(Parameters.Algorithms.DIJKSTRA_BI).traversalMode(tMode).weighting(weighting).
		   build();
		RoutingAlgorithm algorithm = pch.createAlgo(queryGraph, algoOpts);
		Path path = algorithm.calcPath(fromQR.getClosestNode(), toQR.getClosestNode());
		return path;
	}
	
	private void mainLol(String[] args) {
        /*MyGraphHopper graphHopper = new MyGraphHopper();
        graphHopper.init(CmdArgs.read(args));
        graphHopper.importOrLoad();

        GHResponse rsp = new GHResponse();
        List<Path> paths = graphHopper.calcPaths(new GHRequest(52.498668, 13.431473, 52.48947, 13.404007).
                setWeighting("fastest").setVehicle("car"), rsp);
        Path path0 = paths.get(0);
        for (EdgeIteratorState edge : path0.calcEdges()) {
            int edgeId = edge.getEdge();
            String vInfo = "";
            if (edge instanceof VirtualEdgeIteratorState) {
                // first, via and last edges can be virtual
                VirtualEdgeIteratorState vEdge = (VirtualEdgeIteratorState) edge;
                edgeId = vEdge.getOriginalTraversalKey() / 2;
                vInfo = "v";
            }

            System.out.println("(" + vInfo + edgeId + ") " + graphHopper.getOSMWay(edgeId) + ", " + edge.fetchWayGeometry(2));
        }*/
    }
	
	private GHPoint geoPosToGhPoint(GeoPosition position) {
		return new GHPoint(position.getLatitude(), position.getLongitude());
	}
	
	private void drawRoute(List<Painter<JXMapViewer>> painters, PointList route) {
		List<GeoPosition> track = new ArrayList<GeoPosition>();
		for (GHPoint3D point : route) {
			track.add(new GeoPosition(point.lat, point.lon));
		}
		//System.out.println(accessor.getHighways(route));
		drawLines(painters, track);
	}
	
	private void drawLights(List<Painter<JXMapViewer>> painters, List<OSMNode> lights) {
		List<GeoPosition> track = new ArrayList<GeoPosition>();
		for (OSMNode light : lights) {
			track.add(new GeoPosition(light.getLat(), light.getLon()));
		}
		//System.out.println(accessor.getHighways(route));
		drawPoints(painters, track);
	}
	
	private void drawLines(List<Painter<JXMapViewer>> painters, List<GeoPosition> track) {
        RoutePainter routePainter = new RoutePainter(track);
        painters.add(routePainter);
	}
	
	private void drawPoints(List<Painter<JXMapViewer>> painters, List<GeoPosition> track) {
        WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<Waypoint>();
        waypointPainter.setWaypoints(getWaypoints(track));
        painters.add(waypointPainter);
	}
	
	private Set<Waypoint> getWaypoints(List<GeoPosition> track) {
		Set<Waypoint> set = new HashSet<>();
		for (GeoPosition pos : track) {
			set.add(new DefaultWaypoint(pos.getLatitude(), pos.getLongitude()));
		}
		return set;
	}
}
