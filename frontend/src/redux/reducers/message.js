import { CAR_CREATED, CAR_UPDATED, LIGHTS_CREATED } from "../constants";

// Just for reference - defined in store.js
const initialState = {
  lights: [],
  cars: [],
};

const message = (state = initialState, action) => {
  switch (action.type) {
    case LIGHTS_CREATED: {
      const { lights } = action.payload;
      return { ...state, lights: lights };
    }

    case CAR_CREATED: {
      const { car } = action.payload;
      return { ...state, cars: [...state.cars, car] };
    }

    case CAR_UPDATED: {
      const { car } = action.payload;
      return { ...state, cars: state.cars.map((oldCar, i) => (i === car.id ? { ...oldCar, location: car.location } : oldCar)) };
    }

    default:
      return state;
  }
};

export default message;
