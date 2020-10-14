import { CAR_KILLED, CAR_UPDATED, CAR_CREATED, CENTER_UPDATED, SIMULATION_PREPARED, LIGHTS_SWITCHED } from "./constants";
import { createAction } from "redux-actions";

/**
 * @param {{ lat: number; lng:number; rad:number }} center
 */
export const centerUpdated = center => {
  return {
    type: CENTER_UPDATED,
    payload: {
      center,
    },
  };
};

export const simulationPrepared = createAction(
  SIMULATION_PREPARED,
  simulationData => simulationData,
  () => ({ lights: [], stations: [] })
);

/**
 * @param {{id:number; location:{lat:number; lng:number;}; route:Array<> isTestCar:boolean; }} car
 */
export const carCreated = car => {
  return {
    type: CAR_CREATED,
    payload: {
      car,
    },
  };
};

/**
 * @param {{id:number; location:{lat:number; lng:number;}; }} car
 */
export const carUpdated = createAction(CAR_UPDATED);

export const carKilled = createAction(CAR_KILLED);

export const lightsSwitched = createAction(LIGHTS_SWITCHED);
