import { createStore } from "redux";
import { composeWithDevTools } from "redux-devtools-extension";
import {
  D_TP_STRATEGY_ACTIVE,
  D_STATION_STRATEGY_ACTIVE,
  D_PEDS_NUM,
  D_TEST_PED,
  D_CARS_NUM,
  D_TEST_CAR,
  D_GENERATE_CARS,
  D_GENERATE_TP,
  D_TJ_STRATEGY_ACTIVE,
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
  D_USE_FIXED_ROUTES,
  D_USE_FIXED_TP,
  D_GENERATE_BATCHES_FOR_CARS,
  D_GENERATE_BUS_FAILURES,
  D_DETECT_TRAFFIC_JAMS,
  D_TRANSPORT_CHANGE_STRATEGY_ACTIVE,
} from "../constants/defaults";
import { loadLocalData } from "./dataUtils/helpers";
import { ConfigState } from "./models/states";
import appReducer from "./reducers/index";

const initialState = {
  interaction: {
    configState: ConfigState.Initial,
    prepareSimulationData: {
      center: { lat: D_LAT, lng: D_LNG, rad: D_RAD },
      generatePedestrians: D_GENERATE_PEDS,
    },
    startSimulationData: {
      generateCars: D_GENERATE_CARS,
      carsLimit: D_CARS_NUM,
      testCarId: D_TEST_CAR,
      generateBatchesForCars: D_GENERATE_BATCHES_FOR_CARS,

      generateBikes: D_GENERATE_BIKES,
      bikesLimit: D_BIKES_NUM,
      testBikeId: D_TEST_BIKE,

      pedLimit: D_PEDS_NUM,
      testPedId: D_TEST_PED,

      generateTroublePoints: D_GENERATE_TP,
      timeBeforeTrouble: D_TIME_BEFORE_TROUBLE,

      generateBusFailures: D_GENERATE_BUS_FAILURES,
      detectTrafficJams: D_DETECT_TRAFFIC_JAMS,

      useFixedRoutes: D_USE_FIXED_ROUTES,
      useFixedTroublePoints: D_USE_FIXED_TP,

      startTime: D_START_TIME,
      timeScale: D_TIME_SCALE,

      lightStrategyActive: D_LIGHT_STRATEGY_ACTIVE,
      extendLightTime: D_EXTEND_LIGHT_TIME,

      stationStrategyActive: D_STATION_STRATEGY_ACTIVE,
      extendWaitTime: D_EXTEND_WAIT_TIME,

      troublePointStrategyActive: D_TP_STRATEGY_ACTIVE,
      trafficJamStrategyActive: D_TJ_STRATEGY_ACTIVE,
      transportChangeStrategyActive: D_TRANSPORT_CHANGE_STRATEGY_ACTIVE,
    },
  },
  message: {
    lights: [],
    stations: [],
    troublePoints: [],
    inPreparation: false,
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

const interactionState = initialState.interaction;
const data = loadLocalData();
if (data) {
  interactionState.prepareSimulationData = { ...interactionState.prepareSimulationData, ...data.prepareSimulationData };
  interactionState.startSimulationData = { ...interactionState.startSimulationData, ...data.startSimulationData };
  console.info("Set initial data from localStorage");
}

const store = createStore(appReducer, initialState, composeWithDevTools());
export const { dispatch } = store;

export default store;
