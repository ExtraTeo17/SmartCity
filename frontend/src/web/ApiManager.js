import { PREPARE_SIMULATION_REQUEST, START_SIMULATION_REQUEST, DEBUG_REQUEST } from "./MessageType";
import WebServer from "./WebServer";

/** PUBLIC INTERFACE ---------------------------------------------------------- */

export default {
  prepareSimulation({ lat, lng, rad, generatePedestrians } = { lat: 0, lng: 0, rad: 0, generatePedestrians: false }) {
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
    carsLimit,
    testCarId,
    generateCars,
    generateTrafficJams,
    generateTroublePoints,
    timeBeforeTrouble,
    startTime,
    lightStrategyActive,
    extendLightTime,
    stationStrategyActive,
    extendWaitTime,
    changeRouteStrategyActive,
    pedLimit,
    testPedId,
  }) {
    const msg = {
      type: START_SIMULATION_REQUEST,
      payload: {
        carsLimit,
        testCarId,
        generateCars,
        generateTrafficJams,
        generateTroublePoints,
        timeBeforeTrouble,
        startTime,
        lightStrategyActive,
        extendLightTime,
        stationStrategyActive,
        extendWaitTime,
        changeRouteStrategyActive,
        pedLimit,
        testPedId,
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
