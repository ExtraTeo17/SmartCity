import { LightColor } from "../../components/Models/LightColor";
import { SIMULATION_PREPARED, LIGHTS_SWITCHED, TROUBLE_POINT_CREATED, SIMULATION_STARTED } from "../constants";

// Just for reference - defined in store.js
const initialState = {
  lights: [],
  stations: [],
  troublePoints: [],
  wasPrepared: false,
  wasStarted: false,
};

const message = (state = initialState, action) => {
  switch (action.type) {
    case SIMULATION_PREPARED: {
      const { lights, stations } = action.payload;
      return {
        ...state,
        lights,
        stations,
        wasPrepared: true,
      };
    }

    case SIMULATION_STARTED: {
      return { ...state, wasStarted: true };
    }

    case LIGHTS_SWITCHED: {
      const id = action.payload;

      const newLights = state.lights.map(oldLight => {
        if (oldLight.groupId === id) {
          return { ...oldLight, color: oldLight.color === LightColor.GREEN ? LightColor.RED : LightColor.GREEN };
        }

        return oldLight;
      });

      return {
        ...state,
        lights: newLights,
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
