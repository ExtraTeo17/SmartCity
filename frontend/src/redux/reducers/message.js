import { notify } from "react-notify-toast";
import { NOTIFY_SHOW_MS } from "../../utils/constants";
import { LightColor } from "../../components/Models/LightColor";
import { getResultObj } from "../dataUtils/helpers";
import {
  SIMULATION_PREPARED,
  LIGHTS_SWITCHED,
  TROUBLE_POINT_CREATED,
  SIMULATION_STARTED,
  TROUBLE_POINT_VANISHED,
  CAR_KILLED,
} from "../core/constants";

// Just for reference - defined in store.js
const initialState = {
  lights: [],
  stations: [],
  troublePoints: [],
  wasPrepared: false,
  wasStarted: false,
  timeScale: 10,
  timeResults: [],
};

function onKilled(state, data, type) {
  if (data.travelTime) {
    notify.show(`New result for ${type}!`, "success", NOTIFY_SHOW_MS);
    const resultObj = getResultObj(type, data);
    return { ...state, timeResults: [...state.timeResults, resultObj] };
  }

  return state;
}

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

    case CAR_KILLED: {
      return onKilled(state, action.payload, "car");
    }

    default:
      return state;
  }
};

export default message;
