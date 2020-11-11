import { createStore } from "redux";
import { composeWithDevTools } from "redux-devtools-extension";
import {
  D_CHANGE_ROUTE_TP_ACTIVE,
  D_STATION_STRATEGY_ACTIVE,
  D_PEDS_NUM,
  D_TEST_PED,
  D_CARS_NUM,
  D_TEST_CAR,
  D_GENERATE_CARS,
  D_GENERATE_TP,
  D_CHANGE_ROUTE_TJ_ACTIVE,
  D_TIME_BEFORE_TROUBLE,
  D_LIGHT_STRATEGY_ACTIVE,
  D_EXTEND_LIGHT_TIME,
  D_EXTEND_WAIT_TIME,
  D_START_TIME,
  D_TIME_SCALE,
  D_GENERATE_BIKES,
  D_BIKES_NUM,
  D_TEST_BIKE,
  D_RAD,
  D_LNG,
  D_LAT,
  D_GENERATE_PEDS,
} from "../constants/defaults";
import { StartState } from "./models/startState";
import appReducer from "./reducers/index";

const initialState = {
  interaction: {
    shouldStart: StartState.Initial,
    prepareSimulationData: {
      center: { lat: D_LAT, lng: D_LNG, rad: D_RAD },
      generatePedestrians: D_GENERATE_PEDS,
    },
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
      timeBeforeTrouble: D_TIME_BEFORE_TROUBLE,

      startTime: D_START_TIME,
      timeScale: D_TIME_SCALE,

      lightStrategyActive: D_LIGHT_STRATEGY_ACTIVE,
      extendLightTime: D_EXTEND_LIGHT_TIME,

      stationStrategyActive: D_STATION_STRATEGY_ACTIVE,
      extendWaitTime: D_EXTEND_WAIT_TIME,

      changeRouteOnTroublePoint: D_CHANGE_ROUTE_TP_ACTIVE,
      changeRouteOnTrafficJam: D_CHANGE_ROUTE_TJ_ACTIVE,
    },
  },
  message: {
    lights: [],
    stations: [],
    troublePoints: [],
    wasPrepared: 0,
    wasStarted: false,
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
