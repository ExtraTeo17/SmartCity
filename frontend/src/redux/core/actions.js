import { createAction } from "redux-actions";
import {
  CAR_KILLED,
  CAR_UPDATED,
  CAR_CREATED,
  CENTER_UPDATED,
  CENTER_MENU_UPDATED,
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
  PEDESTRIAN_PULLED,
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
} from "./constants";

export const configReplaced = createAction(CONFIG_REPLACED);

/**
 * @param {{ lat: number; lng:number; rad:number }} center
 */
export const centerUpdated = createAction(CENTER_UPDATED);

export const centerMenuUpdated = createAction(CENTER_MENU_UPDATED);

export const startSimulationDataUpdated = createAction(START_SIMULATION_DATA_UPDATED);

export const shouldStartSimulation = createAction(SHOULD_START_SIMULATION);

export const generatePedestriansUpdated = createAction(GENERATE_PEDESTRIANS_UPDATED);

export const simulationPrepared = createAction(
  SIMULATION_PREPARED,
  simulationData => simulationData,
  () => ({ lights: [], stations: [], buses: [] })
);

export const simulationStarted = createAction(SIMULATION_STARTED);

/**
 * @param {{id:number; location:{lat:number; lng:number;}; route:Array<> isTestCar:boolean; }} car
 */
export const carCreated = createAction(CAR_CREATED);
export const carUpdated = createAction(CAR_UPDATED);
export const carKilled = createAction(CAR_KILLED);
export const carRouteChanged = createAction(CAR_ROUTE_CHANGED);

export const lightsSwitched = createAction(LIGHTS_SWITCHED);
export const troublePointCreated = createAction(TROUBLE_POINT_CREATED);
export const troublePointVanished = createAction(TROUBLE_POINT_VANISHED);
export const trafficJamStarted = createAction(TRAFFIC_JAM_STARTED);
export const trafficJamEnded = createAction(TRAFFIC_JAM_ENDED);

export const busUpdated = createAction(BUS_UPDATED);
export const busFillStateUpdated = createAction(BUS_FILL_STATE_UPDATED);
export const busKilled = createAction(BUS_KILLED);

export const pedestrianCreated = createAction(PEDESTRIAN_CREATED);
export const pedestrianUpdated = createAction(PEDESTRIAN_UPDATED);
export const pedestrianPushedIntoBus = createAction(PEDESTRIAN_PUSHED);
export const pedestrianPulledFromBus = createAction(PEDESTRIAN_PULLED);
export const pedestrianKilled = createAction(PEDESTRIAN_KILLED);

export const bikeCreated = createAction(BIKE_CREATED);
export const bikeUpdated = createAction(BIKE_UPDATED);
export const bikeKilled = createAction(BIKE_KILLED);
