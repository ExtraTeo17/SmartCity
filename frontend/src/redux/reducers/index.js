import { combineReducers } from "redux";
import interaction from "./interaction";
import message from "./message";
import car from "./car";
import bus from "./bus";
import pedestrian from "./pedestrian";

// Read this: https://redux.js.org/basics/reducers
// https://redux.js.org/tutorials/essentials/part-1-overview-concepts

const appReducer = combineReducers({
  interaction,
  message,
  car,
  bus,
  pedestrian,
});

export default appReducer;
