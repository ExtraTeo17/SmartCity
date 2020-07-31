import React from "react";
import CityMap from "./CityMap";
import "../styles/App.css";
import Menu from "./Menu";

import appReducer from "../redux/reducers";
import { createStore } from "redux";
import { composeWithDevTools } from "redux-devtools-extension";
import { Provider } from "react-redux";

const store = createStore(appReducer, composeWithDevTools());

const App = () => (
  <Provider store={store}>
    <div className="App">
      <header className="App-header">
        <div className="row w-100 main-container">
          <div className="col-9">
            <CityMap />
          </div>
          <div className="col-3">
            <Menu />
          </div>
        </div>
      </header>
    </div>
  </Provider>
);

export default App;
