import { createStore } from "redux";
import { composeWithDevTools } from "redux-devtools-extension";
import appReducer from "./reducers/index";

const DEFAULT_CARS_NUM = 4;
const DEFAULT_TEST_CAR = 2;

const initialState = {
  interaction: {
    center: {
      lat: 52.23682,
      lng: 21.01681,
      rad: 600,
    },
    startSimulationData: {
      carsNum: DEFAULT_CARS_NUM,
      testCarNum: DEFAULT_TEST_CAR,
      generateCars: true,
      generateTroublePoints: false,
      timeBeforeTrouble: 5,
      time: new Date(),
    },
  },
  message: {
    lights: [],
    cars: [],
    stations: [],
    buses: [],
    pedestrians: [],
    troublePoints: [],
    wasPrepared: false,
    wasStarted: false,
  },
};

const store = createStore(appReducer, initialState, composeWithDevTools());
export const { dispatch } = store;

export default store;
