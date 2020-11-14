import { combineReducers } from "redux";
import interaction from "./interaction";
import message from "./message";
import car from "./agents/car";
import bus from "./agents/bus";
import pedestrian from "./agents/pedestrian";
import bike from "./agents/bike";

// Read this: https://redux.js.org/basics/reducers
// https://redux.js.org/tutorials/essentials/part-1-overview-concepts

const appReducer = combineReducers({
  interaction,
  message,
  car,
  bus,
  pedestrian,
  bike,
});

export default appReducer;
