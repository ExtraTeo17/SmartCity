import { LightColor } from "../../components/Models/LightColor";
import {
  CAR_KILLED,
  CAR_CREATED,
  CAR_UPDATED,
  SIMULATION_PREPARED,
  LIGHTS_SWITCHED,
  TROUBLE_POINT_CREATED,
  SIMULATION_STARTED,
  CAR_ROUTE_CHANGED,
} from "../constants";

// Just for reference - defined in store.js
const initialState = {
  lights: [],
  cars: [],
  stations: [],
  troublePoints: [],
  wasPrepared: false,
  wasStarted: false,
};

const message = (state = initialState, action) => {
  const payload = action.payload;
  switch (action.type) {
    case SIMULATION_PREPARED: {
      const { lights, stations } = action.payload;
      return { ...state, lights: lights, stations: stations, wasPrepared: true };
    }

    case SIMULATION_STARTED: {
      return { ...state, wasStarted: true };
    }

    case CAR_CREATED: {
      const { car } = action.payload;
      return { ...state, cars: [...state.cars, car] };
    }

    case CAR_UPDATED: {
      const car = action.payload;

      let unrecognized = true;
      const newCars = state.cars
        .filter(c => !c.isDeleted)
        .map(c => {
          if (c.id === car.id) {
            unrecognized = false;
            return { ...c, location: car.location };
          }
          return c;
        });

      if (unrecognized === true) {
        newCars.push(car);
      }

      return { ...state, cars: newCars };
    }

    case CAR_KILLED: {
      const id = action.payload;
      let newCars = state.cars.map(c => {
        if (c.id == id) c.isDeleted = true;
        return c;
      });

      return { ...state, cars: newCars };
    }

    case CAR_ROUTE_CHANGED: {
      const id = payload.id;
      let newCars = state.cars.map(c => {
        if (c.id == id) c.route = payload.route;
        return c;
      });

      return { ...state, cars: newCars };
    }

    case LIGHTS_SWITCHED: {
      const id = action.payload;

      return {
        ...state,
        lights: state.lights.map(oldLight =>
          oldLight.groupId === id
            ? { ...oldLight, color: oldLight.color === LightColor.GREEN ? LightColor.RED : LightColor.GREEN }
            : oldLight
        ),
      };
    }

    case TROUBLE_POINT_CREATED: {
      const troublePoint = action.payload;

      return { ...state, troublePoints: [...state.troublePoints, troublePoint] };
    }

    default:
      return state;
  }
};

export default message;
