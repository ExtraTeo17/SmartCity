import { LightColor } from "../../components/Models/LightColor";
import {
  SIMULATION_PREPARED,
  LIGHTS_SWITCHED,
  TROUBLE_POINT_CREATED,
  SIMULATION_STARTED,
  TROUBLE_POINT_VANISHED,
} from "../constants";

// Just for reference - defined in store.js
const initialState = {
  lights: [],
  stations: [],
  troublePoints: [],
  wasPrepared: false,
  wasStarted: false,
  timeScale: 10,
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
        wasStarted: false,
      };
    }

    case SIMULATION_STARTED: {
      const timeScale = action.payload;
      return { ...state, wasStarted: true, timeScale };
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

    case TROUBLE_POINT_VANISHED: {
      const id = action.payload;

      console.log(`Handling tp-hide: ${id}`);
      return { ...state, troublePoints: state.troublePoints.filter(tp => tp.id !== id) };
    }

    default:
      return state;
  }
};

export default message;
