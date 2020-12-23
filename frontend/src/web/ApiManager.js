import { PREPARE_SIMULATION_REQUEST, START_SIMULATION_REQUEST, DEBUG_REQUEST } from "./MessageType";
import WebServer from "./WebServer";

/**
 * Used for sending outgoing messsages
 * @category Web
 * @module ApiManager
 */

/**
 * @typedef {Object} Zone - Represents circle on map
 * @property {number} lat - Latitude in degrees
 * @property {number} lng - Longitude in degrees
 * @property {number} rad - Radius in meters
 */

/**
 * @typedef {Object} PrepareSimulationData
 * @property {Zone} center - Zone data
 * @property {Boolean} generatePedestrians - If should generate buses, stations and pedestrians
 */

/**
 * @typedef {Object} StartSimulationData - Data object
 * @property {Boolean} generateCars - If should generate cars
 * @property {Number} carsLimit - Cars generation limit
 * @property {Number} testCarId - Number of testCar, `testCarId - 1` cars will be generated before it
 * @property {Boolean} generateBatchesForCars - If should generate same or random ways for each car
 * @property {Boolean} generateBikes - If should generate bikes
 * @property {Number} bikesLimit - Bikes generation limit
 * @property {Number} testBikeId - Number of test-bike, `testBikeId - 1` bikes will be generated before it
 * @property {Number} pedLimit - Pedestrians generation limit
 * @property {Number} testPedId - Number of test-pedestrians, `testPedId - 1` pedestrians will be generated before it
 * @property {Boolean} generateTroublePoints - If should generate trouble points
 * @property {Number} timeBeforeTrouble - Time before trouble point occurs for each car
 * @property {Boolean} detectTrafficJams - If should detect traffic jams
 * @property {Boolean} generateBusFailures - If should generate bus failures
 * @property {Boolean} useFixedRoutes - If should use same routes for cars / bikes / pedestrians (with fixed seed)
 * @property {Boolean} useFixedTroublePoints - If should use same trouble points positions
 * @property {Date} startTime - Start time of the simulation
 * @property {Number} timeScale - Time scale for simulation clock
 * @property {Boolean} lightStrategyActive - If should use lights strategy
 * @property {Number} extendLightTime - Extend time for which light stays green
 * @property {Boolean} stationStrategyActive - If should use stations strategy
 * @property {Number} extendWaitTime - Extend time for each bus additionally waits for pedestrian
 * @property {Boolean} troublePointStrategyActive - If should use trouble points strategy
 * @property {Boolean} trafficJamStrategyActive - If should use traffic jams strategy
 * @property {Boolean} transportChangeStrategyActive - If should use transport change strategy
 */

export default {
  /**
   * Returns state of connection
   * @returns {Boolean} True if connected, false otherwise
   */
  isConnected() {
    return WebServer.isConnected();
  },

  /**
   * Sends 'prepareSimulation' message to server
   * @param {PrepareSimulationData} prepareSimulationData - Data object
   */
  prepareSimulation({ center: { lat, lng, rad }, generatePedestrians }) {
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

  /**
   * Sends 'startSimulation' message to server
   * @param {StartSimulationData} startSimulationData - Data object
   */
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

  /**
   * Sends debug message with empty paylod
   */
  debug() {
    const msg = {
      type: DEBUG_REQUEST,
      payload: {},
    };
    WebServer.send(msg);
  },
};
