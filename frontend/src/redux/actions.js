import { CAR_UPDATED, CAR_CREATED, CENTER_UPDATED, LIGHTS_CREATED } from "./constants";

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
 * @param {{id:number; location:{lat:number; lng:number;}; isTestCar:boolean; }} car
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
export const carUpdated = car => {
  return {
    type: CAR_UPDATED,
    payload: {
      car,
    },
  };
};
