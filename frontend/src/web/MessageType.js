/**
 * Message type constants
 * @category Web
 * @module MessageType
 */

/**
 * @constant
 * @default
 */
const PREPARE_SIMULATION_REQUEST = 1;

/**
 * @constant
 * @default
 */
const PREPARE_SIMULATION_RESPONSE = 2;

/**
 * @constant
 * @default
 */
const START_SIMULATION_REQUEST = 3;

/**
 * @constant
 * @default
 */
const START_SIMULATION_RESPONSE = 4;

/**
 * @constant
 * @default
 */
const DEBUG_REQUEST = 5;

/**
 * @constant
 * @default
 */
const CREATE_CAR_INFO = 10;

/**
 * @constant
 * @default
 */
const UPDATE_CAR_INFO = 11;

/**
 * @constant
 * @default
 */
const KILL_CAR_INFO = 12;

/**
 * @constant
 * @default
 */
const UPDATE_CAR_ROUTE_INFO = 13;

/**
 * @constant
 * @default
 */
const SWITCH_LIGHTS_INFO = 20;

/**
 * @constant
 * @default
 */
const CREATE_TROUBLE_POINT_INFO = 21;

/**
 * @constant
 * @default
 */
const HIDE_TROUBLE_POINT_INFO = 22;

/**
 * @constant
 * @default
 */
const START_TRAFFIC_JAM_INFO = 23;

/**
 * @constant
 * @default
 */
const END_TRAFFIC_JAM_INFO = 24;

/**
 * @constant
 * @default
 */
const BATCHED_UPDATE_INFO = 25;

/**
 * @constant
 * @default
 */
const API_OVERLOAD_INFO = 26;

/**
 * @constant
 * @default
 */
const UPDATE_BUS_INFO = 30;

/**
 * @constant
 * @default
 */
const UPDATE_BUS_FILL_STATE_INFO = 31;

/**
 * @constant
 * @default
 */
const KILL_BUS_INFO = 32;

/**
 * @constant
 * @default
 */
const CRASH_BUS_INFO = 33;

/**
 * @constant
 * @default
 */
const CREATE_PEDESTRIAN_INFO = 40;

/**
 * @constant
 * @default
 */
const UPDATE_PEDESTRIAN_INFO = 41;

/**
 * @constant
 * @default
 */
const PUSH_PEDESTRIAN_INTO_BUS_INFO = 42;

/**
 * @constant
 * @default
 */
const PULL_PEDESTRIAN_AWAY_FROM_BUS_INFO = 43;

/**
 * @constant
 * @default
 */
const KILL_PEDESTRIAN_INFO = 44;

/**
 * @constant
 * @default
 */
const CREATE_BIKE_INFO = 50;

/**
 * @constant
 * @default
 */
const UPDATE_BIKE_INFO = 51;

/**
 * @constant
 * @default
 */
const KILL_BIKE_INFO = 52;

/**
 * @typedef {number} MessageType
 * @enum {MessageType}
 */
export {
  PREPARE_SIMULATION_REQUEST,
  PREPARE_SIMULATION_RESPONSE,
  START_SIMULATION_REQUEST,
  START_SIMULATION_RESPONSE,
  DEBUG_REQUEST,
  CREATE_CAR_INFO,
  UPDATE_CAR_INFO,
  KILL_CAR_INFO,
  UPDATE_CAR_ROUTE_INFO,
  SWITCH_LIGHTS_INFO,
  CREATE_TROUBLE_POINT_INFO,
  HIDE_TROUBLE_POINT_INFO,
  START_TRAFFIC_JAM_INFO,
  END_TRAFFIC_JAM_INFO,
  BATCHED_UPDATE_INFO,
  UPDATE_BUS_INFO,
  UPDATE_BUS_FILL_STATE_INFO,
  KILL_BUS_INFO,
  CREATE_PEDESTRIAN_INFO,
  UPDATE_PEDESTRIAN_INFO,
  PUSH_PEDESTRIAN_INTO_BUS_INFO,
  PULL_PEDESTRIAN_AWAY_FROM_BUS_INFO,
  KILL_PEDESTRIAN_INFO,
  CREATE_BIKE_INFO,
  UPDATE_BIKE_INFO,
  KILL_BIKE_INFO,
  CRASH_BUS_INFO,
  API_OVERLOAD_INFO,
};
