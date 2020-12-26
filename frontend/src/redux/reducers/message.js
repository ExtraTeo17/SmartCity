import { notify } from "react-notify-toast";
import { NOTIFY_SHOW_MS } from "../../constants/global";
import { LightColor } from "../../components/Models/LightColor";
import { getResultObj } from "../dataUtils/dataUtils";
import { getRandomInt } from "../../utils/helpers";
import {
  SIMULATION_PREPARE_STARTED,
  SIMULATION_PREPARED,
  LIGHTS_SWITCHED,
  TROUBLE_POINT_CREATED,
  SIMULATION_STARTED,
  TROUBLE_POINT_VANISHED,
  CAR_KILLED,
  BIKE_KILLED,
  PEDESTRIAN_KILLED,
  TRAFFIC_JAM_STARTED,
  TRAFFIC_JAM_ENDED,
} from "../core/constants";

/**
 * Handles interaction with from server, i.e:
 *  - SIMULATION_PREPARED
 *  - SIMULATION_STARTED
 *  - LIGHTS_SWITCHED
 *  - TROUBLE_POINT_CREATED
 *  - TRAFFIC_JAM_(STARTED|ENDED)
 *  - (CAR|BIKE|PEDESTRIAN)_KILLED - for test objects results
 * @category Redux
 * @subcategory Reducers
 * @module message
 */

// Just for reference - defined in store.js
const initialState = {
  lights: [],
  stations: [],
  troublePoints: [],
  inPreparation: false,
  wasPrepared: 0,
  wasStarted: false,
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
    case SIMULATION_PREPARE_STARTED: {
      return { ...state, inPreparation: true };
    }

    case SIMULATION_PREPARED: {
      const { lights, stations } = action.payload;
      return {
        ...state,
        lights,
        stations,
        inPreparation: false,
        wasPrepared: state.wasPrepared + 1,
        wasStarted: false,
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
      const isConstruction = getRandomInt(0, 1) % 2 === 0;
      const troublePoint = { ...action.payload, isConstruction };

      return { ...state, troublePoints: [...state.troublePoints, troublePoint] };
    }

    case TROUBLE_POINT_VANISHED: {
      const id = action.payload;

      return { ...state, troublePoints: state.troublePoints.filter(tp => tp.id !== id) };
    }

    case TRAFFIC_JAM_STARTED: {
      const id = action.payload;

      const newLights = state.lights.map(l => {
        if (l.id === id) {
          return { ...l, jammed: true };
        }
        return l;
      });

      return { ...state, lights: newLights };
    }

    case TRAFFIC_JAM_ENDED: {
      const id = action.payload;

      const newLights = state.lights.map(l => {
        if (l.id === id) {
          return { ...l, jammed: false };
        }
        return l;
      });

      return { ...state, lights: newLights };
    }

    case CAR_KILLED: {
      return onKilled(state, action.payload, "Car");
    }

    case BIKE_KILLED: {
      return onKilled(state, action.payload, "Bike");
    }

    case PEDESTRIAN_KILLED: {
      return onKilled(state, action.payload, "Pedestrian");
    }

    default:
      return state;
  }
};

export default message;
