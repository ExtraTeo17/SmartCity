import { createStore } from "redux";
import { composeWithDevTools } from "redux-devtools-extension";
import appReducer from "./reducers/index";

const initialState = {
  interaction: {
    center: {
      lat: 52.23682,
      lng: 21.01681,
      rad: 600,
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
