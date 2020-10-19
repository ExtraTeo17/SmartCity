import appReducer from "./reducers/index";
import { createStore } from "redux";
import { composeWithDevTools } from "redux-devtools-extension";

const initialState = {
  interaction: {
    center: {
      lat: 52.203,
      lng: 20.861,
      rad: 600,
    },
  },
  message: {
    lights: [],
    cars: [],
    stations: [],
    buses: [],
    troublePoints: [],
    wasPrepared: false,
    wasStarted: false,
  },
};

const store = createStore(appReducer, initialState, composeWithDevTools());
export const dispatch = store.dispatch;

export default store;
