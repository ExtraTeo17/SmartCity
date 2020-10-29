import {
  CENTER_UPDATED,
  GENERATE_PEDESTRIANS_UPDATED,
  SHOULD_START_SIMULATION,
  START_SIMULATION_DATA_UPDATED,
} from "../core/constants";
import {
  D_CHANGE_ROUTE_STRATEGY_ACTIVE,
  D_STATION_STRATEGY_ACTIVE,
  D_PEDS_NUM,
  D_TEST_PED,
  D_GENERATE_CARS,
  D_CARS_NUM,
  D_TEST_CAR,
  D_GENERATE_BIKES,
  D_BIKES_NUM,
  D_TEST_BIKE,
  D_GENERATE_TP,
  D_GENERATE_TJ,
  D_TIME_BEFORE_TROUBLE,
  D_LIGHT_STRATEGY_ACTIVE,
  D_EXTEND_LIGHT_TIME,
  D_EXTEND_WAIT_TIME,
  D_START_TIME,
} from "../../constants/defaults";
import { StartState } from "../models/startState";

// Just for reference - defined in store.js
const initialState = {
  center: { lat: 0, lng: 0, rad: 0 },
  generatePedestrians: false,
  shouldStart: StartState.Initial,
  startSimulationData: {
    pedLimit: D_PEDS_NUM,
    testPedId: D_TEST_PED,

    generateCars: D_GENERATE_CARS,
    carsLimit: D_CARS_NUM,
    testCarId: D_TEST_CAR,

    generateBikes: D_GENERATE_BIKES,
    bikesLimit: D_BIKES_NUM,
    testBikeId: D_TEST_BIKE,

    generateTroublePoints: D_GENERATE_TP,
    generateTrafficJams: D_GENERATE_TJ,
    timeBeforeTrouble: D_TIME_BEFORE_TROUBLE,

    startTime: D_START_TIME,

    lightStrategyActive: D_LIGHT_STRATEGY_ACTIVE,
    extendLightTime: D_EXTEND_LIGHT_TIME,

    stationStrategyActive: D_STATION_STRATEGY_ACTIVE,
    extendWaitTime: D_EXTEND_WAIT_TIME,

    changeRouteStrategyActive: D_CHANGE_ROUTE_STRATEGY_ACTIVE,
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
