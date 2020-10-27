import { createStore } from "redux";
import { composeWithDevTools } from "redux-devtools-extension";
import { StartState } from "./models/startState";
import appReducer from "./reducers/index";

const initialState = {
  interaction: {
    center: {
      lat: 52.23682,
      lng: 21.01681,
      rad: 600,
    },
    generatePedestrians: false,
    shouldStart: StartState.Initial,
    startSimulationData: {
      pedLimit: 0,
      testPedId: 0,

      carsLimit: 0,
      testCarId: 0,
      generateCars: true,
      generateTroublePoints: false,
      timeBeforeTrouble: 5,

      startTime: new Date(),

      lightStrategyActive: true,
      extendLightTime: true,

      stationStrategyActive: true,
      extendWaitTime: 30,

      changeRouteStrategyActive: true,
    },
  },
  message: {
    lights: [],
    stations: [],
    troublePoints: [],
    wasPrepared: false,
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
};

const store = createStore(appReducer, initialState, composeWithDevTools());
export const { dispatch } = store;

export default store;
