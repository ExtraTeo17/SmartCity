import { CAR_KILLED, CAR_CREATED, CAR_UPDATED, LIGHT_LOCATIONS_UPDATED } from "../constants";

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

    case CAR_UPDATED: {
      const { car } = action.payload;
      return { ...state, cars: state.cars.map((oldCar, i) => (i === car.id ? { ...oldCar, location: car.location } : oldCar)) };
    }

    case CAR_KILLED: {
      const id = action.payload;
      let newCars = state.cars.slice();
      newCars.forEach(c => {
        if (c.id === id) c.isDeleted = true;
      });

      return { ...state, cars: newCars };
    }

    default:
      return state;
  }
};

export default message;
