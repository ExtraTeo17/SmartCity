import { combineReducers } from "redux";
import interaction from "./interaction";
import message from "./message";

// Read this: https://redux.js.org/basics/reducers
// https://redux.js.org/tutorials/essentials/part-1-overview-concepts

const appReducer = combineReducers({
  interaction,
  message,
});

export default appReducer;
