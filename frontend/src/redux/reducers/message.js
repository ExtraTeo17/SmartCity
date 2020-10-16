import { LightColor } from "../../components/Models/LightColor";
import { CAR_KILLED, CAR_CREATED, CAR_UPDATED, SIMULATION_PREPARED, LIGHTS_SWITCHED, TROUBLE_POINT_CREATED } from "../constants";

// Just for reference - defined in store.js
const initialState = {
  lights: [],
  cars: [],
  stations: [],
  troublePoints: [],
};

const message = (state = initialState, action) => {
  switch (action.type) {
    case SIMULATION_PREPARED: {
      const { lights, stations } = action.payload;
      return { ...state, lights: lights, stations: stations };
    }

    case CAR_CREATED: {
      const { car } = action.payload;
      return { ...state, cars: [...state.cars, car] };
    }

    case CAR_UPDATED: {
      const car = action.payload;

      var newCars = state.cars.map(c => {
        if (c.id === car.id && !c.isDeleted) {
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

    case TROUBLE_POINT_CREATED: {
      const troublePoint = action.payload;

      return { ...state, troublePoints: [...state.troublePoints, troublePoint] };
    }

    default:
      return state;
  }
};

export default message;
