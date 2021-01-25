import { CAR_KILLED, CAR_CREATED, CAR_UPDATED, CAR_ROUTE_CHANGED, BATCHED_UPDATE } from "../../core/constants";

/**
 * Handles car-agent-related interaction
 *  - CAR_CREATED
 *  - CAR_UPDATED
 *  - BATCHED_UPDATE
 *  - CAR_ROUTE_CHANGED
 *  - CAR_KILLED
 *
 * @category Redux
 * @subcategory Reducers
 * @module car
 */

// Just for reference - defined in store.js
const initialState = {
  cars: [],
};

const deletedCarIds = [];

const car = (state = initialState, action) => {
  const { payload } = action;
  switch (action.type) {
    case CAR_CREATED: {
      const car = payload;
      return { ...state, cars: [...state.cars, car] };
    }

    case CAR_UPDATED: {
      const car = payload;

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

    case BATCHED_UPDATE: {
      const { carUpdates } = payload;

      const newCars = state.cars.map(c => {
        const update = carUpdates.find(car => car.id === c.id);
        if (update) {
          return { ...c, location: update.location };
        }
        return c;
      });

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

    case CAR_KILLED: {
      const { id } = action.payload;
      const newCars = state.cars.filter(c => c.id !== id);
      deletedCarIds.push(id);

      return { ...state, cars: newCars };
    }

    default:
      return state;
  }
};

export default car;
