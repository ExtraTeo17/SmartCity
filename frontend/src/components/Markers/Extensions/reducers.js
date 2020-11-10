import { angleFromCoordinates } from "../../../utils/helpers";

export const getRotationReducer = angleThreshold => {
  return function rotationReducer(state = { loc: 0, angle: 0 }, action) {
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
