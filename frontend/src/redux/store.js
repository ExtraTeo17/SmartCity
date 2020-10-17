import appReducer from "./reducers/index";
import { createStore } from "redux";
import { composeWithDevTools } from "redux-devtools-extension";

const initialState = {
  interaction: {
    center: {
      lat: 52.30667,
      lng: 20.9343,
      rad: 1000,
    },
  },
  message: {
    lights: [],
    cars: [],
    stations: [],
    troublePoints: [],
  },
};

const store = createStore(appReducer, initialState, composeWithDevTools());
export const dispatch = store.dispatch;

export default store;
