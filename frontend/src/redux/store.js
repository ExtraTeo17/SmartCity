import { createStore } from "redux";
import { composeWithDevTools } from "redux-devtools-extension";
import {
  D_CHANGE_ROUTE_STRATEGY_ACTIVE,
  D_STATION_STRATEGY_ACTIVE,
  D_PEDS_NUM,
  D_TEST_PED,
  D_CARS_NUM,
  D_TEST_CAR,
  D_GENERATE_CARS,
  D_GENERATE_TP,
  D_GENERATE_TJ,
  D_TIME_BEFORE_TROUBLE,
  D_LIGHT_STRATEGY_ACTIVE,
  D_EXTEND_LIGHT_TIME,
  D_EXTEND_WAIT_TIME,
  D_START_TIME,
} from "../constants/defaults";
import { StartState } from "./models/startState";
import appReducer from "./reducers/index";

const initialState = {
  interaction: {
    center: {
      lat: 52.23698,
      lng: 21.01766,
      rad: 130,
    },
    generatePedestrians: false,
    shouldStart: StartState.Initial,
    startSimulationData: {
      pedLimit: D_PEDS_NUM,
      testPedId: D_TEST_PED,

      carsLimit: D_CARS_NUM,
      testCarId: D_TEST_CAR,
      generateCars: D_GENERATE_CARS,
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
  },
  message: {
    lights: [],
    stations: [],
    troublePoints: [],
    wasPrepared: 0,
    wasStarted: false,
    timeScale: 10,
    timeResults: [],
  },
  bus: {
    buses: [],
  },
  pedestrian: {
    pedestrians: [],
  },
  car: {
    cars: [],
  },
  bike: {
    bikes: [],
  },
};

const store = createStore(appReducer, initialState, composeWithDevTools());
export const { dispatch } = store;

export default store;
