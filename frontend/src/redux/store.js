import { createStore } from "redux";
import { composeWithDevTools } from "redux-devtools-extension";
import appReducer from "./reducers/index";

const initialState = {
  interaction: {
    center: {
      lat: 52.203,
      lng: 20.861,
      rad: 350,
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
