import { LightColor } from "../../components/Models/LightColor";
import { CAR_KILLED, CAR_CREATED, CAR_UPDATED, LIGHTS_CREATED, LIGHTS_SWITCHED } from "../constants";

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

      var newCars = state.cars.map(c => {
        if (c.id == car.id && !c.isDeleted) {
          return { ...c, location: car.location };
        }
        return c;
      });

      return { ...state, cars: newCars };
    }

    case CAR_KILLED: {
      const id = action.payload;
      let newCars = state.cars.slice();
      newCars.forEach(c => {
        if (c.id === id) c.isDeleted = true;
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

    default:
      return state;
  }
};

export default message;
