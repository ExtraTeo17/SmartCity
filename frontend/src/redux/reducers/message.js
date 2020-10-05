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
      const { car } = action.payload;
      return { ...state, cars: [...state.cars, car] };
    }

    default:
      return state;
  }
};

export default message;
