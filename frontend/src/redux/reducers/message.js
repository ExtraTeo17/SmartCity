import { LIGHT_LOCATIONS_UPDATED } from "../constants";

const initialState = {
  lightLocations: [],
};

const message = (state = initialState, action) => {
  switch (action.type) {
    case LIGHT_LOCATIONS_UPDATED: {
      const { lightLocations } = action.payload;
      return { ...state, lightLocations: lightLocations.slice() };
    }

    default:
      return state;
  }
};

export default message;
