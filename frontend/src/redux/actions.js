import { createAction } from "redux-actions";
import {
  CAR_KILLED,
  CAR_UPDATED,
  CAR_CREATED,
  CENTER_UPDATED,
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
} from "./constants";

/**
 * @param {{ lat: number; lng:number; rad:number }} center
 */
export const centerUpdated = createAction(CENTER_UPDATED);

export const simulationPrepared = createAction(
  SIMULATION_PREPARED,
  simulationData => simulationData,
  () => ({ lights: [], stations: [], buses: [] })
);

export const simulationStarted = createAction(SIMULATION_STARTED);

/**
 * @param {{id:number; location:{lat:number; lng:number;}; route:Array<> isTestCar:boolean; }} car
 */
export const carCreated = car => ({
  type: CAR_CREATED,
  payload: {
    car,
  },
});

/**
 * @param {{id:number; location:{lat:number; lng:number;}; }} car
 */
export const carUpdated = createAction(CAR_UPDATED);
export const carKilled = createAction(CAR_KILLED);
export const carRouteChanged = createAction(CAR_ROUTE_CHANGED);

export const lightsSwitched = createAction(LIGHTS_SWITCHED);
export const troublePointCreated = createAction(TROUBLE_POINT_CREATED);
export const troublePointVanished = createAction(TROUBLE_POINT_VANISHED);

export const busUpdated = createAction(BUS_UPDATED);
export const busFillStateUpdated = createAction(BUS_FILL_STATE_UPDATED);
export const busKilled = createAction(BUS_KILLED);

export const pedestrianCreated = createAction(PEDESTRIAN_CREATED);
export const pedestrianUpdated = createAction(PEDESTRIAN_UPDATED);
export const pedestrianPushedIntoBus = createAction(PEDESTRIAN_PUSHED);
export const pedestrianPulledFromBus = createAction(PEDESTRIAN_PULLED);
export const pedestrianKilled = createAction(PEDESTRIAN_KILLED);
