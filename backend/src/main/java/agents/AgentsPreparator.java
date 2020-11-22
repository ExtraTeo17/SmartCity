package agents;

import agents.abstractions.IAgentsContainer;
import agents.abstractions.IAgentsFactory;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import events.ClearSimulationEvent;
import events.LightManagersReadyEvent;
import events.web.PrepareSimulationEvent;
import events.web.SimulationPreparedEvent;

import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import osmproxy.abstractions.ICacheWrapper;
import osmproxy.abstractions.ILightAccessManager;
import osmproxy.abstractions.IMapAccessManager;
import osmproxy.buses.BusInfo;
import osmproxy.buses.Timetable;
import osmproxy.buses.abstractions.IBusLinesManager;
import osmproxy.buses.data.BusPreparationData;
import osmproxy.elements.OSMNode;
import osmproxy.elements.OSMStation;
import osmproxy.elements.OSMWay;
import routing.abstractions.IRouteGenerator;
import routing.nodes.RouteNode;
import routing.nodes.StationNode;
import smartcity.SimulationState;
import smartcity.TimeProvider;
import smartcity.config.ConfigContainer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static smartcity.config.StaticConfig.USE_DEPRECATED_XML_FOR_LIGHT_MANAGERS;

@SuppressWarnings("OverlyCoupledClass")
public class AgentsPreparator {
    private static final Logger logger = LoggerFactory.getLogger(AgentsPreparator.class);
    private final IAgentsContainer agentsContainer;
    private final ConfigContainer configContainer;
    private final IBusLinesManager busLinesManager;
    private final IAgentsFactory factory;
    private final EventBus eventBus;
    private final ILightAccessManager lightAccessManager;
    private final IMapAccessManager mapAccessManager;
    private final IRouteGenerator routeGenerator;
    private final ICacheWrapper cacheWrapper;

    @Inject
    public AgentsPreparator(IAgentsContainer agentsContainer,
                            ConfigContainer configContainer,
                            IBusLinesManager busLinesManager,
                            IAgentsFactory factory,
                            EventBus eventBus,
                            ILightAccessManager lightAccessManager,
                            IMapAccessManager mapAccessManager,
                            IRouteGenerator routeGenerator,
                            ICacheWrapper cacheWrapper) {
        this.agentsContainer = agentsContainer;
        this.configContainer = configContainer;
        this.busLinesManager = busLinesManager;
        this.factory = factory;
        this.eventBus = eventBus;
        this.lightAccessManager = lightAccessManager;
        this.mapAccessManager = mapAccessManager;
        this.routeGenerator = routeGenerator;
        this.cacheWrapper = cacheWrapper;
    }


    @SuppressWarnings("FeatureEnvy")
    @Subscribe
    public void handle(PrepareSimulationEvent e) {
        logger.info(PrepareSimulationEvent.class.getSimpleName() + " event occurred: " + e.toString());
        var state = configContainer.getSimulationState();
        if (state == SimulationState.READY_TO_RUN || state == SimulationState.RUNNING) {
            clear();
        }

        configContainer.setZone(e.zone);
        configContainer.setGeneratePedestriansAndBuses(e.shouldGeneratePedestriansAndBuses);
        configContainer.setSimulationState(SimulationState.IN_PREPARATION);

        if (prepareAgents()) {
            configContainer.setSimulationState(SimulationState.READY_TO_RUN);

            var lights = agentsContainer.stream(LightManagerAgent.class)
                    .flatMap(man -> man.getLights().stream())
                    .collect(Collectors.toList());
            var stations = agentsContainer.stream(StationAgent.class).map(
                    StationAgent::getStation).collect(Collectors.toList());
            var buses = agentsContainer.stream(BusAgent.class).map(
                    BusAgent::getBus).collect(Collectors.toList());
            eventBus.post(new SimulationPreparedEvent(lights, stations, buses));
        }
    }

    private void clear() {
        eventBus.post(new ClearSimulationEvent());
        agentsContainer.clearAll();
    }

    private boolean prepareAgents() {
        if (configContainer.shouldGeneratePedestriansAndBuses()) {
            if (!prepareStationsAndBuses()) {
                return false;
            }
        }
        return prepareLightManagers();
    }

    private boolean prepareStationsAndBuses() {
        var busData = getBusPreparationData();

        logger.info("Stations creation started.");
        long time = System.nanoTime();
        var stationNodes = prepareStations(busData.stations.values());
        if (stationNodes.size() == 0) {
            return false;
        }
        logger.info("Stations are created! Took: " + TimeProvider.getTimeInMs(time) + "ms\n");

        logger.info("Buses creation started.");
        time = System.nanoTime();
        if (!preparesBuses(busData, stationNodes)) {
            return false;
        }
        logger.info("Buses are created! Took: " + TimeProvider.getTimeInMs(time) + "ms\n");

        return true;
    }

    private BusPreparationData getBusPreparationData() {
        BusPreparationData busData;
        var cachedData = cacheWrapper.getBusPreparationData();
        if (cachedData.isPresent()) {
            busData = cachedData.get();
        }
        else {
            logger.info("Starting bus data preparation.");
            long time = System.nanoTime();
            busData = busLinesManager.getBusData();
            logger.info("Bus data preparation finished! Took: " + TimeProvider.getTimeInMs(time) + "ms\n");
            cacheWrapper.cacheData(busData);
        }

        return busData;
    }

    private List<StationNode> prepareStations(Collection<OSMStation> stations) {
        int stationsCount = 0;
        List<StationNode> stationNodes = new ArrayList<>();
        for (var station : stations) {
            StationAgent agent = factory.create(station);
            boolean result = agentsContainer.tryAdd(agent);
            if (result) {
                ++stationsCount;
                agent.start();
                // Should probably be moved to nodesCreator if any extensions will be needed
                stationNodes.add(new StationNode(agent.getStation(), agent.getId()));
            }
            else {
                logger.info("Station agent could not be added");
            }
        }

        if (stationsCount == 0) {
            logger.error("No stations were created");
            return stationNodes;
        }

        logger.info("NUMBER OF STATION AGENTS: " + stationsCount);
        return stationNodes;
    }

    private boolean preparesBuses(BusPreparationData busData, List<StationNode> allStations) {
        int busCount = 0;
        var closestTime = LocalDateTime.now().plusDays(1);
        var currTime = LocalDateTime.now();
        prepareBusManagerAgent(busData.busInfos);
        Set<Pair<Pair<String, String>, String>> addedLinesBrigades = new HashSet<>();
        for (var busInfo : busData.busInfos) {
            var busLine = busInfo.busLine;
            var route = getBusRoute(busInfo.route, busInfo.stops, allStations,busLine);
            for (var brigade : busInfo) {
                var brigadeNr = brigade.brigadeId;
                for (Timetable timetable : brigade) {
                	Pair<String, String> lineBrigade = Pair.with(busLine, brigadeNr);
                	Pair<Pair<String, String>, String> lbTable = Pair.with(lineBrigade, timetable.getBoardingTime().toString());
                	if (addedLinesBrigades.contains(lbTable)) {
                		continue;
                	}
            		addedLinesBrigades.add(lbTable);
                    BusAgent agent = factory.create(route, timetable, busLine, brigadeNr);
                    boolean result = agentsContainer.tryAdd(agent, true);
                    if (result) {
                        ++busCount;
                    }
                    else {
                        logger.warn("Bus agent could not be added");
                    }

                    var startTime = timetable.getBoardingTime();
                    closestTime = TimeProvider.getCloser(currTime, closestTime, startTime);
                }
            }
        }

        if (busCount == 0) {
            logger.error("No buses were created");
            return false;
        }
        logger.info("Closest startTime: " + closestTime.toLocalTime() + "\n" +
                "    NUMBER OF BUS AGENTS: " + busCount);
        return true;
    }

    private void prepareBusManagerAgent(HashSet<BusInfo> busInfos) {
		BusManagerAgent agent = factory.create(busInfos);
		boolean result = agentsContainer.tryAdd(agent, true);
		if (!result) {
			logger.error("BusManagerAgent was not be created");
		}
		agent.start();
	}


	private List<RouteNode> getBusRoute(List<OSMWay> osmRoute, List<OSMStation> osmStops,
                                        List<StationNode> allStations,String busLine) {
        List<StationNode> mergedStationNodes = new ArrayList<>(osmStops.size());
        List<OSMStation> mergedOsmStops = new ArrayList<>(osmStops.size());

        for (var osmStop : osmStops) {
            var stopId = osmStop.getId();
            var station = allStations.stream().filter(node -> node.getOsmId() == stopId).findAny();
            if (station.isPresent()) {
                mergedStationNodes.add(station.get());
                mergedOsmStops.add(osmStop);
            }
            else {
                logger.error("Stop present on way is not initiated as StationAgent: " + osmStop);
            }
        }
        addInfoAboutAllBusStopsOnTheRouteAndBusLine(mergedOsmStops,busLine,mergedStationNodes);
        var timeNow = System.nanoTime();
        var route = routeGenerator.generateRouteInfoForBuses(osmRoute, mergedStationNodes);
        logger.info("Generating routeInfo finished. Took: " + (TimeProvider.getTimeInMs(timeNow)) + "ms");

        return route;
    }

    private void addInfoAboutAllBusStopsOnTheRouteAndBusLine( List<OSMStation> osmStops,
                                                             String busLine,
                                                             List<StationNode> mergedStationNodes) {

       // var stationIds = mergedStationNodes.stream().map(node -> node.getOsmId()).collect(Collectors.toList());
        for (OSMStation el : osmStops)
        {
            el.addToBusLineStopMap(busLine, mergedStationNodes);
        }


    }


    private boolean prepareLightManagers() {
        logger.info("LightManagers construction started.");
        long time = System.nanoTime();
        boolean result = tryConstructLightManagers();
        logger.info("LightManagers construction finished! Took: " + TimeProvider.getTimeInMs(time) + "ms");

        if (result) {
            var lightManagers = ImmutableList.copyOf(agentsContainer.iterator(LightManagerAgent.class));
            eventBus.post(new LightManagersReadyEvent(lightManagers));
        }

        return result;
    }

    @VisibleForTesting
    boolean tryConstructLightManagers() {
        int managersCounter = 0;
        try {
            if (USE_DEPRECATED_XML_FOR_LIGHT_MANAGERS) {
                var nodes = mapAccessManager.getLightManagersNodes(configContainer.getZone());
                for (var node : nodes) {
                    //noinspection deprecation
                    var manager = factory.create(node);
                    if (agentsContainer.tryAdd(manager)) {
                        ++managersCounter;
                    }
                }
            } else {
                var lights = lightAccessManager.getLightsOfTypeA();
                for (final OSMNode centerCrossroad : lights) {
                    if (centerCrossroad.determineParentOrientationsTowardsCrossroad()) {
                        var manager = factory.create(centerCrossroad);
                        if (agentsContainer.tryAdd(manager)) {
                            ++managersCounter;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error preparing light managers", e);
            return false;
        }

        if (managersCounter == 0) {
            logger.warn("No managers were created");
        }


        return true;
    }
}
