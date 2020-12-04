import { PREPARE_SIMULATION_REQUEST, START_SIMULATION_REQUEST, DEBUG_REQUEST } from "./MessageType";
import WebServer from "./WebServer";

/** PUBLIC INTERFACE ---------------------------------------------------------- */

export default {
  prepareSimulation(
    { center: { lat, lng, rad }, generatePedestrians } = { center: { lat: 0, lng: 0, rad: 0 }, generatePedestrians: false }
  ) {
    const msg = {
      type: PREPARE_SIMULATION_REQUEST,
      payload: {
        latitude: lat,
        longitude: lng,
        radius: rad,
        generatePedestrians,
      },
    };
    WebServer.send(msg);
  },

  startSimulation({
    generateCars,
    carsLimit,
    testCarId,
    generateBatchesForCars,

    generateBikes,
    bikesLimit,
    testBikeId,

    pedLimit,
    testPedId,

    generateTroublePoints,
    timeBeforeTrouble,

    generateBusFailures,
    detectTrafficJams,

    useFixedRoutes,
    useFixedTroublePoints,

    startTime,
    timeScale,

    lightStrategyActive,
    extendLightTime,

    stationStrategyActive,
    extendWaitTime,

    troublePointStrategyActive,
    trafficJamStrategyActive,
    transportChangeStrategyActive,
  }) {
    const msg = {
      type: START_SIMULATION_REQUEST,
      payload: {
        generateCars,
        carsLimit,
        testCarId,
        generateBatchesForCars,

        generateBikes,
        bikesLimit,
        testBikeId,

        pedLimit,
        testPedId,

        generateTroublePoints,
        timeBeforeTrouble,

        generateBusFailures,
        detectTrafficJams,

        useFixedRoutes,
        useFixedTroublePoints,

        startTime,
        timeScale,

        lightStrategyActive,
        extendLightTime,

        stationStrategyActive,
        extendWaitTime,

        troublePointStrategyActive,
        trafficJamStrategyActive,
        transportChangeStrategyActive,
      },
    };
    WebServer.send(msg);
  },

  debug() {
    const msg = {
      type: DEBUG_REQUEST,
      payload: {},
    };
    WebServer.send(msg);
  },
};
