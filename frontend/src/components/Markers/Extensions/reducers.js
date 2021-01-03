import { angleFromCoordinates } from "../../../utils/helpers";

/**
 * @category Markers
 * @subcategory Other
 * @module Extensions
 */

/**
 * Used to create reducer with specific rotation threshold
 * @function getRotationReducer
 * @param {number} angleThreshold
 */
export const getRotationReducer = angleThreshold => {
  return function rotationReducer(state = { loc: { lat: 0, lng: 0 }, angle: 0 }, action) {
    const newLocation = action.payload;
    if (state.loc !== newLocation) {
      const newAngle = angleFromCoordinates(state.loc, newLocation);
      if (Math.abs(newAngle - state.angle) > angleThreshold) {
        return { loc: newLocation, angle: newAngle };
      }

      return { ...state, loc: newLocation };
    }

    return state;
  };
};
