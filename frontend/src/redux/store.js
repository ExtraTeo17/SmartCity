import appReducer from "./reducers/index";
import { createStore } from "redux";
import { composeWithDevTools } from "redux-devtools-extension";

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
    troublePoints: [],
  },
};

const store = createStore(appReducer, initialState, composeWithDevTools());
export const dispatch = store.dispatch;

export default store;
