import {
  CENTER_UPDATED,
  GENERATE_PEDESTRIANS_UPDATED,
  SHOULD_START_SIMULATION,
  START_SIMULATION_DATA_UPDATED,
  CONFIG_REPLACED,
  CENTER_MENU_UPDATED,
} from "../core/constants";
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
} from "../../constants/defaults";
import { ConfigState, getNextConfigState } from "../models/states";
import { createLocalDataObject, saveLocalData } from "../dataUtils/helpers";

/**
 * Handles interaction with user interface
 * @category Redux
 * @subcategory Reducers
 * @module interaction
 */

// Just for reference - defined in store.js
export const initialInteractionState = {
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
};

const interaction = (state = initialInteractionState, action) => {
  switch (action.type) {
    case CENTER_UPDATED: {
      const center = action.payload;
      const oldData = state.prepareSimulationData;
      return {
        ...state,
        configState: getNextConfigState(state.configState),
        prepareSimulationData: { ...oldData, center: { ...oldData.center, ...center } },
      };
    }

    // To avoid re-rendering of prepare-menu components
    case CENTER_MENU_UPDATED: {
      const center = action.payload;
      const oldData = state.prepareSimulationData;
      return {
        ...state,
        prepareSimulationData: { ...oldData, center: { ...oldData.center, ...center } },
      };
    }

    case GENERATE_PEDESTRIANS_UPDATED: {
      const generatePedestrians = action.payload;
      return { ...state, prepareSimulationData: { ...state.prepareSimulationData, generatePedestrians } };
    }

    case START_SIMULATION_DATA_UPDATED: {
      const data = action.payload;
      return { ...state, startSimulationData: { ...state.startSimulationData, ...data } };
    }

    case SHOULD_START_SIMULATION: {
      saveLocalData(createLocalDataObject(state));

      return state;
    }

    case CONFIG_REPLACED: {
      const newConfig = action.payload;
      console.group("Config");
      console.info(newConfig);
      console.groupEnd();

      return { ...newConfig, configState: getNextConfigState(state.configState) };
    }

    default:
      return state;
  }
};

export default interaction;
