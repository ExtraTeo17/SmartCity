import { CAR_KILLED, CAR_UPDATED, CAR_CREATED, CENTER_UPDATED, LIGHTS_CREATED, LIGHTS_SWITCHED } from "./constants";
import { createAction } from "redux-actions";

export /**
 * @param {} center
 */
/**
 * @param {{ lat: number; lng:number; rad:number }} center
 */
const centerUpdated = center => {
  return {
    type: CENTER_UPDATED,
    payload: {
      center,
    },
  };
};

export /**
 * @param {Array<{{id:number; location:{{lat: number; lng:number}}; color:number;}}>} lights
 */
const lightsCreated = lights => {
  return {
    type: LIGHTS_CREATED,
    payload: {
      lights: lights,
    },
  };
};

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
