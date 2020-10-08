import { LightColor } from "../../components/Models/LightColor";
import { CAR_CREATED, CAR_UPDATED, LIGHTS_CREATED, LIGHTS_SWITCHED } from "../constants";

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
      return {
        ...state,
        cars: state.cars.map(oldCar => (oldCar.id === car.id ? { ...oldCar, location: car.location } : oldCar)),
      };
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

    default:
      return state;
  }
};

export default message;
