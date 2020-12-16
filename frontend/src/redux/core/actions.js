import { createAction } from "redux-actions";
import {
  CAR_KILLED,
  CAR_UPDATED,
  CAR_CREATED,
  CENTER_UPDATED,
  CENTER_MENU_UPDATED,
  SIMULATION_PREPARE_STARTED,
  SIMULATION_PREPARED,
  SIMULATION_STARTED,
  LIGHTS_SWITCHED,
  TROUBLE_POINT_CREATED,
  CAR_ROUTE_CHANGED,
  BUS_UPDATED,
  BUS_FILL_STATE_UPDATED,
  BUS_KILLED,
  PEDESTRIAN_CREATED,
  PEDESTRIAN_UPDATED,
  PEDESTRIAN_PUSHED,
  PEDESTRIAN_PULLED_AWAY,
  PEDESTRIAN_KILLED,
  TROUBLE_POINT_VANISHED,
  START_SIMULATION_DATA_UPDATED,
  SHOULD_START_SIMULATION,
  GENERATE_PEDESTRIANS_UPDATED,
  TRAFFIC_JAM_STARTED,
  TRAFFIC_JAM_ENDED,
  BIKE_KILLED,
  BIKE_UPDATED,
  BIKE_CREATED,
  CONFIG_REPLACED,
  BATCHED_UPDATE,
  BUS_CRASHED,
} from "./constants";

/**
 * Defines actions which pass data for reducers
 * @category Redux
 * @subcategory Core
 * @module actions
 */

/**
 * @function
 */
export const configReplaced = createAction(CONFIG_REPLACED);

/**
 * @function
 */
export const centerUpdated = createAction(CENTER_UPDATED);

/**
 * @function
 */
export const centerMenuUpdated = createAction(CENTER_MENU_UPDATED);

/**
 * @function
 */
export const startSimulationDataUpdated = createAction(START_SIMULATION_DATA_UPDATED);

/**
 * @function
 */
export const shouldStartSimulation = createAction(SHOULD_START_SIMULATION);

/**
 * @function
 */
export const generatePedestriansUpdated = createAction(GENERATE_PEDESTRIANS_UPDATED);

/**
 * @function
 */
export const simulationPrepareStarted = createAction(SIMULATION_PREPARE_STARTED);

/**
 * @function
 */
export const simulationPrepared = createAction(
  SIMULATION_PREPARED,
  simulationData => simulationData,
  () => ({ lights: [], stations: [], buses: [] })
);

/**
 * @function
 */
export const simulationStarted = createAction(SIMULATION_STARTED);

/**
 * @function
 * @param {{ id: Number, location: Location, route:Array<Location>, isTestCar:Boolean }} car
 */
export const carCreated = createAction(CAR_CREATED);
/**
 * @function
 */
export const carUpdated = createAction(CAR_UPDATED);
/**
 * @function
 */
export const carKilled = createAction(CAR_KILLED);
/**
 * @function
 */
export const carRouteChanged = createAction(CAR_ROUTE_CHANGED);

/**
 * @function
 */
export const lightsSwitched = createAction(LIGHTS_SWITCHED);
/**
 * @function
 */
export const troublePointCreated = createAction(TROUBLE_POINT_CREATED);
/**
 * @function
 */
export const troublePointVanished = createAction(TROUBLE_POINT_VANISHED);
/**
 * @function
 */
export const trafficJamStarted = createAction(TRAFFIC_JAM_STARTED);
/**
 * @function
 */
export const trafficJamEnded = createAction(TRAFFIC_JAM_ENDED);
/**
 * @function
 */
export const batchedUpdate = createAction(BATCHED_UPDATE);

/**
 * @function
 */
export const busUpdated = createAction(BUS_UPDATED);
/**
 * @function
 */
export const busFillStateUpdated = createAction(BUS_FILL_STATE_UPDATED);
/**
 * @function
 */
export const busKilled = createAction(BUS_KILLED);
/**
 * @function
 */
export const busCrashed = createAction(BUS_CRASHED);

/**
 * @function
 */
export const pedestrianCreated = createAction(PEDESTRIAN_CREATED);
/**
 * @function
 */
export const pedestrianUpdated = createAction(PEDESTRIAN_UPDATED);
/**
 * @function
 */
export const pedestrianPushedIntoBus = createAction(PEDESTRIAN_PUSHED);
/**
 * @function
 */
export const pedestrianPulledAwayFromBus = createAction(PEDESTRIAN_PULLED_AWAY);
/**
 * @function
 */
export const pedestrianKilled = createAction(PEDESTRIAN_KILLED);

/**
 * @function
 */
export const bikeCreated = createAction(BIKE_CREATED);
/**
 * @function
 */
export const bikeUpdated = createAction(BIKE_UPDATED);
/**
 * @function
 */
export const bikeKilled = createAction(BIKE_KILLED);
