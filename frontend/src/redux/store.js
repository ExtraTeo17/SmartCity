import appReducer from "./reducers/index";
import { createStore } from "redux";
import { composeWithDevTools } from "redux-devtools-extension";

const store = createStore(appReducer, composeWithDevTools());
export const dispatch = store.dispatch;

export default store;
