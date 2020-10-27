import {
  CENTER_UPDATED,
  GENERATE_PEDESTRIANS_UPDATED,
  SHOULD_START_SIMULATION,
  START_SIMULATION_DATA_UPDATED,
} from "../core/constants";
import { StartState } from "../models/startState";

// Just for reference - defined in store.js
const initialState = {
  center: { lat: 0, lng: 0, rad: 0 },
  generatePedestrians: false,
  shouldStart: StartState.Initial,
  startSimulationData: {
    carsLimit: 0,
    testCarId: 0,
    generateCars: true,
    generateTroublePoints: false,
    timeBeforeTrouble: 5,

    pedLimit: 0,
    testPedId: 0,

    startTime: new Date(),
  },
};

function getNextState(oldState) {
  switch (oldState) {
    case StartState.Initial:
      return StartState.Invoke;
    case StartState.Invoke:
      return StartState.Proceed;

    default:
      return StartState.Initial;
  }
}

/**
 * @param {{ type: any; payload: { center: { lat: number; lng: number; rad: number}; }; }} action
 */
const interaction = (state = initialState, action) => {
  switch (action.type) {
    case CENTER_UPDATED: {
      const center = action.payload;
      return { ...state, center: { ...state.center, ...center } };
    }

    case GENERATE_PEDESTRIANS_UPDATED: {
      const generatePedestrians = action.payload;
      return { ...state, generatePedestrians };
    }

    case START_SIMULATION_DATA_UPDATED: {
      const data = action.payload;
      return { ...state, startSimulationData: { ...state.startSimulationData, ...data } };
    }

    case SHOULD_START_SIMULATION: {
      const newState = getNextState(state.shouldStart);
      return { ...state, shouldStart: newState };
    }

    default:
      return state;
  }
};

export default interaction;
