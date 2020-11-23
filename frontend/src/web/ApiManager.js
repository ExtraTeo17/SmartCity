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
    pedLimit,
    testPedId,

    generateCars,
    carsLimit,
    testCarId,

    generateBikes,
    bikesLimit,
    testBikeId,

    changeRouteOnTrafficJam,
    generateTroublePoints,
    timeBeforeTrouble,

    useFixedRoutes,
    useFixedTroublePoints,

    startTime,
    timeScale,

    lightStrategyActive,
    extendLightTime,

    stationStrategyActive,
    extendWaitTime,

    changeRouteOnTroublePoint,
  }) {
    const msg = {
      type: START_SIMULATION_REQUEST,
      payload: {
        pedLimit,
        testPedId,

        generateCars,
        carsLimit,
        testCarId,

        generateBikes,
        bikesLimit,
        testBikeId,

        generateTroublePoints,
        timeBeforeTrouble,

        useFixedRoutes,
        useFixedTroublePoints,

        startTime,
        timeScale,

        lightStrategyActive,
        extendLightTime,

        stationStrategyActive,
        extendWaitTime,

        changeRouteOnTroublePoint,
        changeRouteOnTrafficJam,
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
