import { CENTER_UPDATED, LIGHT_LOCATIONS_UPDATED } from "./constants";

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
 * @param {Array} lightLocations
 */
const lightLocationsUpdated = lightLocations => {
  return {
    type: LIGHT_LOCATIONS_UPDATED,
    payload: {
      lightLocations,
    },
  };
};
