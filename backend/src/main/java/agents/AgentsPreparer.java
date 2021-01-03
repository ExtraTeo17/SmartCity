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
import utilities.ConditionalExecutor;
import utilities.Siblings;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static smartcity.config.StaticConfig.USE_DEPRECATED_XML_FOR_LIGHT_MANAGERS;

/**
 * Prepares all agents that need more time to be created
 */
@SuppressWarnings("OverlyCoupledClass")
public class AgentsPreparer {
    private static final Logger logger = LoggerFactory.getLogger(AgentsPreparer.class);
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
    public AgentsPreparer(IAgentsContainer agentsContainer,
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
        logger.info("Stations are created! Took: " + TimeProvider.getTimeInMs(time) + "ms\n");

        logger.info("Buses creation started.");
        time = System.nanoTime();
        preparesBuses(busData, stationNodes);
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
            logger.info("Bus data preparation finished! Took: " + TimeProvider.getTimeInMs(time) + "ms");
            cacheWrapper.cacheData(busData);
        }

        return busData;
    }

    private List<StationNode> prepareStations(Collection<OSMStation> stations) {
        List<StationNode> stationNodes = new ArrayList<>();
        for (var stationsForBusStopId : stations.stream()
                .collect(groupingBy(OSMStation::getBusStopNr, groupingBy(OSMStation::getBusStopId)))
                .values()) {
            for (var stationsForBusStopNr : stationsForBusStopId.values()) {
                var stationSiblings = getStationAndItsPlatform(stationsForBusStopNr);
                StationAgent agentMain = factory.create(stationSiblings.first);
                boolean result = agentsContainer.tryAdd(agentMain);
                if (!result) {
                    logger.warn("Station agent could not be added");
                    continue;
                }

                agentMain.start();
                var mainStation = agentMain.getStation();
                if (stationSiblings.first != stationSiblings.second) {
                    var platform = stationSiblings.second;
                    mainStation.setCorrespondingPlatformStation(platform);
                }
                stationNodes.add(mainStation);
            }
        }

        logger.info("Number of station agents: " + stationNodes.size());
        return stationNodes;
    }

    /**
     * @param stationsForBusStopNr - stations for single bus stop [id and nr]
     * @return Stations, where first will not be platform if possible
     */
    private Siblings<OSMStation> getStationAndItsPlatform(List<OSMStation> stationsForBusStopNr) {
        OSMStation main = null;
        OSMStation platform = null;
        for (int i = 0; (main == null || platform == null) && i < stationsForBusStopNr.size(); ++i) {
            var station = stationsForBusStopNr.get(i);
            if (station.isPlatform()) {
                platform = station;
            }
            else {
                main = station;
            }
        }

        if (main == null) {
            main = platform;
        }

        if (stationsForBusStopNr.size() > 2) {
            logger.warn("Stops for bus id size greater than 2!: " + stationsForBusStopNr.size() + " \n"
                    + "Main: " + main + "\n"
                    + "Platform: " + platform + "\n"
                    + "All:\n" + stationsForBusStopNr.stream().map(OSMStation::toString).collect(Collectors.joining(" \n")));
        }

        return Siblings.of(main, platform);
    }

    private void preparesBuses(BusPreparationData busData, List<StationNode> allStations) {
        int busCount = 0;
        var closestTime = LocalDateTime.now().plusDays(1);
        var currTime = LocalDateTime.now();
        prepareBusManagerAgent(busData.busInfos);
        Set<Pair<Pair<String, String>, String>> addedLinesBrigades = new HashSet<>();
        for (var busInfo : busData.busInfos) {
            var busLine = busInfo.busLine;
            var route = getBusRoute(busInfo.route, busInfo.stops, allStations);
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
            logger.warn("No buses were created");
        }

        logger.info("Closest startTime: " + closestTime.toLocalTime() + "\nNumber of bus agents: " + busCount);
    }

    private void prepareBusManagerAgent(HashSet<BusInfo> busInfos) {
        BusManagerAgent agent = factory.create(busInfos);
        boolean result = agentsContainer.tryAdd(agent, true);
        if (!result) {
            logger.error("BusManagerAgent was not added to the main container");
            return;
        }
        agent.start();
    }

    private List<RouteNode> getBusRoute(List<OSMWay> osmRoute, List<OSMStation> osmStops,
                                        List<StationNode> allStations) {
        List<StationNode> mergedStationNodes = new ArrayList<>(osmStops.size());

        for (var osmStop : osmStops) {
            var stopId = osmStop.getId();
            var station = allStations.stream().filter(node -> node.getOsmId() == stopId).findAny();
            if (station.isEmpty()) {
                logger.error("Stop present on way is not initiated as StationAgent:\n " + osmStop);
                ConditionalExecutor.debug(() -> logger.info("All stations:\n" + allStations.stream()
                        .map(StationNode::toString).collect(Collectors.joining("\n"))));
                continue;
            }

            mergedStationNodes.add(station.get());
        }

        var timeNow = System.nanoTime();
        var route = routeGenerator.generateRouteInfoForBuses(osmRoute, mergedStationNodes);
        logger.info("Generating routeInfo finished. Took: " + (TimeProvider.getTimeInMs(timeNow)) + "ms");

        return route;
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
            }
            else {
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
