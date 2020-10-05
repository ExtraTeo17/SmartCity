import { CAR_CREATED, LIGHT_LOCATIONS_UPDATED } from "../constants";

// Just for reference - defined in store.js
const initialState = {
  lightLocations: [],
  cars: [],
};

const message = (state = initialState, action) => {
  switch (action.type) {
    case LIGHT_LOCATIONS_UPDATED: {
      const { lightLocations } = action.payload;
      return { ...state, lightLocations: lightLocations };
    }
    case CAR_CREATED: {
      const { location } = action.payload;
      return { ...state, cars: [...state.cars, { location }] };
    }

    default:
      return state;
  }
};

export default message;
