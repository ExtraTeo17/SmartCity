import { CAR_KILLED, CAR_CREATED, CAR_UPDATED, CAR_ROUTE_CHANGED } from "../constants";

// Just for reference - defined in store.js
const initialState = {
  cars: [],
};

const deletedCarIds = [];

const car = (state = initialState, action) => {
  const { payload } = action;
  switch (action.type) {
    case CAR_CREATED: {
      const { car } = action.payload;
      return { ...state, cars: [...state.cars, car] };
    }

    case CAR_UPDATED: {
      const car = action.payload;

      let unrecognized = true;
      const newCars = state.cars.map(c => {
        if (c.id === car.id) {
          unrecognized = false;
          return { ...c, location: car.location };
        }
        return c;
      });

      if (unrecognized === true && !deletedCarIds.includes(car.id)) {
        newCars.push(car);
      }

      return { ...state, cars: newCars };
    }

    case CAR_KILLED: {
      const id = action.payload;
      const newCars = state.cars.filter(c => c.id !== id);
      deletedCarIds.push(id);

      return { ...state, cars: newCars };
    }

    case CAR_ROUTE_CHANGED: {
      const { id, routeStart, routeEnd, location } = payload;
      const newCars = state.cars.map(c => {
        if (c.id === id) {
          return { ...c, route: [...routeStart, ...routeEnd], routeChangePoint: location };
        }
        return c;
      });

      return { ...state, cars: newCars };
    }

    default:
      return state;
  }
};

export default car;
