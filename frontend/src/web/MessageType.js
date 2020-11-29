const PREPARE_SIMULATION_REQUEST = 1;
const PREPARE_SIMULATION_RESPONSE = 2;
const START_SIMULATION_REQUEST = 3;
const START_SIMULATION_RESPONSE = 4;
const DEBUG_REQUEST = 5;

const CREATE_CAR_INFO = 10;
const UPDATE_CAR_INFO = 11;
const KILL_CAR_INFO = 12;
const UPDATE_CAR_ROUTE_INFO = 13;

const SWITCH_LIGHTS_INFO = 20;
const CREATE_TROUBLE_POINT_INFO = 21;
const HIDE_TROUBLE_POINT_INFO = 22;
const START_TRAFFIC_JAM_INFO = 23;
const END_TRAFFIC_JAM_INFO = 24;
const BATCHED_UPDATE_INFO = 25;

const UPDATE_BUS_INFO = 30;
const UPDATE_BUS_FILL_STATE_INFO = 31;
const KILL_BUS_INFO = 32;

const CREATE_PEDESTRIAN_INFO = 40;
const UPDATE_PEDESTRIAN_INFO = 41;
const PUSH_PEDESTRIAN_INTO_BUS_INFO = 42;
const PULL_PEDESTRIAN_FROM_BUS_INFO = 43;
const KILL_PEDESTRIAN_INFO = 44;

const CREATE_BIKE_INFO = 50;
const UPDATE_BIKE_INFO = 51;
const KILL_BIKE_INFO = 52;

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
  PULL_PEDESTRIAN_FROM_BUS_INFO,
  KILL_PEDESTRIAN_INFO,
  CREATE_BIKE_INFO,
  UPDATE_BIKE_INFO,
  KILL_BIKE_INFO,
};
