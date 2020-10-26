import { PREPARE_SIMULATION_REQUEST, START_SIMULATION_REQUEST } from "./MessageType";
import WebServer from "./WebServer";

/** PUBLIC INTERFACE ---------------------------------------------------------- */

export default {
  prepareSimulation({ lat, lng, rad, generatePedestrians, pedLimit, testPedId } = { lat: 0, lng: 0, rad: 0 }) {
    const msg = {
      type: PREPARE_SIMULATION_REQUEST,
      payload: {
        latitude: lat,
        longitude: lng,
        radius: rad,
        generatePedestrians,
        pedLimit,
        testPedId,
      },
    };
    WebServer.send(msg);
  },

  startSimulation({
    carsLimit,
    testCarId,
    generateCars,
    generateTroublePoints,
    startTime,
    lightStrategyActive,
    extendLightTime,
    stationStrategyActive,
    extendWaitTime,
    changeRouteStrategyActive,
  }) {
    const msg = {
      type: START_SIMULATION_REQUEST,
      payload: {
        carsLimit,
        testCarId,
        generateCars,
        generateTroublePoints,
        startTime,
        lightStrategyActive,
        extendLightTime,
        stationStrategyActive,
        extendWaitTime,
        changeRouteStrategyActive,
      },
    };
    console.log(msg);
    WebServer.send(msg);
  },
};
