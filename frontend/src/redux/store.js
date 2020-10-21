import { createStore } from "redux";
import appReducer from "./reducers/index";
// import { composeWithDevTools } from "redux-devtools-extension";

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
    hiddenPedestrians: [],
    troublePoints: [],
    wasPrepared: false,
    wasStarted: false,
  },
};

const store = createStore(appReducer, initialState);
export const { dispatch } = store;

export default store;
