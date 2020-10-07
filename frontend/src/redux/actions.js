import { CAR_CREATED, CENTER_UPDATED, LIGHT_LOCATIONS_UPDATED } from "./constants";

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
 * @param {Array<{{lat: number, lng:number}}>} lightLocations
 */
const lightLocationsUpdated = lightLocations => {
  return {
    type: LIGHT_LOCATIONS_UPDATED,
    payload: {
      lightLocations,
    },
  };
};

/**
 * @param {{lat:number; lng:number}} location
 */
export const carCreated = car => {
  return {
    type: CAR_CREATED,
    payload: {
      car,
    },
  };
};
