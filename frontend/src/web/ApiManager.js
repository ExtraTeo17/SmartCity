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
 * @property {boolean} generatePedestrians - If should generate buses, stations and pedestrians
 */

/**
 * @typedef {Object} StartSimulationData - Data object
 * @property {boolean} generateCars - If should generate cars
 * @property {number} carsLimit - Cars generation limit
 * @property {number} testCarId - Number of testCar, `testCarId - 1` cars will be generated before it
 * @property {boolean} generateBatchesForCars - If should generate same or random ways for each car
 * @property {boolean} generateBikes - If should generate bikes
 * @property {number} bikesLimit - Bikes generation limit
 * @property {number} testBikeId - Number of test-bike, `testBikeId - 1` bikes will be generated before it
 * @property {number} pedLimit - Pedestrians generation limit
 * @property {number} testPedId - Number of test-pedestrians, `testPedId - 1` pedestrians will be generated before it
 * @property {boolean} generateTroublePoints - If should generate trouble points
 * @property {number} timeBeforeTrouble - Time before trouble point occurs for each car
 * @property {boolean} detectTrafficJams - If should detect traffic jams
 * @property {boolean} generateBusFailures - If should generate bus failures
 * @property {boolean} useFixedRoutes - If should use same routes for cars / bikes / pedestrians (with fixed seed)
 * @property {boolean} useFixedTroublePoints - If should use same trouble points positions
 * @property {Date} startTime - Start time of the simulation
 * @property {number} timeScale - Time scale for simulation clock
 * @property {boolean} lightStrategyActive - If should use lights strategy
 * @property {number} extendLightTime - Extend time for which light stays green
 * @property {boolean} stationStrategyActive - If should use stations strategy
 * @property {number} extendWaitTime - Extend time for each bus additionally waits for pedestrian
 * @property {boolean} troublePointStrategyActive - If should use trouble points strategy
 * @property {number} troublePointThresholdUntilIndexChange - Number of car moves before it changes it's route when trouble point occurs
 * @property {number} noTroublePointStrategyIndexFactor - Number of car moves before car see trouble point
 *  and can change it's route (when strategy is off)
 * @property {boolean} trafficJamStrategyActive - If should use traffic jams strategy
 * @property {boolean} transportChangeStrategyActive - If should use transport change strategy
 */

export default {
  /**
   * Returns state of connection
   * @returns {boolean} True if connected, false otherwise
   */
  isConnected() {
    return WebServer.isConnected();
  },

  /**
   * Sends 'prepareSimulation' message to server
   * @param {PrepareSimulationData} prepareSimulationData - Data object
   */
  prepareSimulation({ center: { lat, lng, rad }, generatePedestrians }) {
    if (!WebServer.isConnected()) {
      console.warn("Server not connected. Cannot the send message");
      return;
    }

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
    troublePointThresholdUntilIndexChange,
    noTroublePointStrategyIndexFactor,

    trafficJamStrategyActive,
    transportChangeStrategyActive,
  }) {
    if (!WebServer.isConnected()) {
      console.warn("Server not connected. Cannot the send message");
      return;
    }

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
        troublePointThresholdUntilIndexChange,
        noTroublePointStrategyIndexFactor,

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
    if (!WebServer.isConnected()) {
      console.warn("Server not connected. Cannot the send message");
      return;
    }

    const msg = {
      type: DEBUG_REQUEST,
      payload: {},
    };
    WebServer.send(msg);
  },
};
