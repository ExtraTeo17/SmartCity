import { areLocationsEqual, calculateDistance } from "../../../utils/helpers";
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

const maxMoveDelayMs = 400;
const printIntervalMs = 5000;

let prevPrintDate = Date.now();
const speeds = [];
const avgSpeeds = [];

function savePartialSpeed(loc1, loc2, delta) {
  const dist = calculateDistance(loc1, loc2);
  // console.log(dist);
  const speed = (1000 * dist) / delta; // speed in m/s
  speeds.push(speed);
}

const sumFunc = (a, b) => a + b;

function computeAverageSpeed(now) {
  if (speeds.length === 0) {
    return;
  }

  const sum = speeds.reduce(sumFunc, 0);
  const avgKmph = (sum * 3600) / (1000 * speeds.length);
  avgSpeeds.push(avgKmph);

  const totalAvg = avgSpeeds.reduce(sumFunc, 0) / avgSpeeds.length;
  console.info(`Avg speed of car = ${totalAvg}`);

  speeds.length = 0;
  prevPrintDate = now;
}

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

      const now = Date.now();

      const newCars = state.cars.map(c => {
        const update = carUpdates.find(car => car.id === c.id);
        if (update && !areLocationsEqual(c.location, update.location)) {
          if (c.time && now - c.time < maxMoveDelayMs) {
            savePartialSpeed(c.location, update.location, now - c.time);
          }

          return { ...c, location: update.location, time: now };
        }

        return c;
      });

      if (now - prevPrintDate > printIntervalMs) {
        computeAverageSpeed(now);
      }

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
