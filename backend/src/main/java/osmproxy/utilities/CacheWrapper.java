package osmproxy.utilities;

import com.google.inject.Inject;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import osmproxy.SimulationData;
import osmproxy.WayWithLights;
import osmproxy.abstractions.ICacheWrapper;
import osmproxy.buses.data.BusPreparationData;
import osmproxy.elements.OSMWay;
import routing.core.IZone;
import routing.nodes.RouteNode;
import routing.nodes.StationNode;
import smartcity.config.ConfigContainer;
import utilities.FileWrapper;

import java.util.*;

import static smartcity.config.StaticConfig.USE_FRESH_DATA;

public class CacheWrapper implements ICacheWrapper {
    private static final Logger logger = LoggerFactory.getLogger(CacheWrapper.class);

    private final ConfigContainer configContainer;
    private final Map<String, Pair<List<OSMWay>, List<StationNode>>> routeFileNamesMap = new HashMap<>();

    @Inject
    public CacheWrapper(ConfigContainer configContainer) {
        this.configContainer = configContainer;
    }

    @Override
    public Optional<BusPreparationData> getBusPreparationData() {
        if (USE_FRESH_DATA) {
            return Optional.empty();
        }

        var cachedData = FileWrapper.<BusPreparationData>getFromCache(getBusDataFileName(configContainer.getZone()));
        if (cachedData == null) {
            return Optional.empty();
        }

        logger.info("Successfully retrieved bus data from cache.");
        return Optional.of(cachedData);
    }

    @Override
    public Optional<SimulationData> getSimulationData() {
    	if (USE_FRESH_DATA) {
    		return Optional.empty();
    	}

    	var cachedData = FileWrapper.<SimulationData>getFromCache(getSimulationDataFileName(configContainer.getZone()));
    	if (cachedData == null) {
    		return Optional.empty();
    	}

    	logger.info("Successfully retrieved SIMULATION data from cache.");
    	return Optional.of(cachedData);
    }

    private String getSimulationDataFileName(IZone zone) {
        var center = zone.getCenter();
        return "simulation_" + zone.getRadius() + "_" + center.getLng() + "_" + center.getLat();
	}

	private static String getBusDataFileName(IZone zone) {
        var center = zone.getCenter();
        return "zone_" + zone.getRadius() + "_" + center.getLng() + "_" + center.getLat();
    }

    @Override
    public void cacheData(BusPreparationData data) {
        var zone = configContainer.getZone();
        String path = getBusDataFileName(zone);
        FileWrapper.cacheToFile(data, path);
    }

    @Override
    public void cacheData(SimulationData data) {
        var zone = configContainer.getZone();
        String path = getSimulationDataFileName(zone);
        FileWrapper.cacheToFile(data, path);
    }

    @Override
    public ArrayList<RouteNode> getBusRoute(List<OSMWay> route,
                                            List<StationNode> stationNodes) {
        if (USE_FRESH_DATA) {
            return new ArrayList<>();
        }

        String cacheFileName = getRouteFileName(route, stationNodes);
        var data = FileWrapper.<ArrayList<RouteNode>>getFromCache(cacheFileName);
        if (data != null) {
            logger.info("Successfully retrieved bus route data from cache.");
            return data;
        }

        return new ArrayList<>();
    }

    private String getRouteFileName(List<OSMWay> route, List<StationNode> stationNodes) {
        var routeHash = route.hashCode();
        var stationsHash = stationNodes.hashCode();

        var fileName = "r_" + routeHash + "_" + stationsHash;
        if (!isFileNameUnique(fileName, route, stationNodes)) {
            logger.error("Filename: " + fileName + " is not unique!!!");
        }

        return fileName;
    }


    private boolean isFileNameUnique(String fileName, List<OSMWay> route, List<StationNode> stations) {
        var pair = routeFileNamesMap.get(fileName);
        if (pair == null) {
            routeFileNamesMap.put(fileName, Pair.with(route, stations));
            return true;
        }

        var cachedRoute = pair.getValue0();
        if (cachedRoute.size() != route.size()) {
            return false;
        }
        for (int i = 0; i < route.size(); ++i) {
            if (route.get(i).getId() != cachedRoute.get(i).getId()) {
                return false;
            }
        }

        var cachedStations = pair.getValue1();
        if (cachedStations.size() != stations.size()) {
            return false;
        }
        for (int i = 0; i < stations.size(); ++i) {
            if (stations.get(i).getOsmId() != cachedStations.get(i).getOsmId()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void cacheData(List<OSMWay> route, List<StationNode> stationNodes, ArrayList<RouteNode> data) {
        String path = getRouteFileName(route, stationNodes);
        FileWrapper.cacheToFile(data, path);
    }
}
