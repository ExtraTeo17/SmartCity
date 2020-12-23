import React from "react";
import { Provider } from "react-redux";
import Notifications from "react-notify-toast";

import CityMap from "./CityMap";
import store from "../redux/store";
import MenusContainer from "./Menu/MenusContainer";

import "../styles/App.css";

/**
 * Central component, which holds both map and menu
 * @category Main
 * @module App
 * @component
 */
const App = () => (
  <Provider store={store}>
    <Notifications />
    <div className="App">
      <header className="App-header">
        <div className="row w-100">
          <div className="col map-container" onContextMenu={e => e.preventDefault()}>
            <CityMap />
          </div>
          <div className="col-3 menu-container">
            <MenusContainer />
          </div>
        </div>
      </header>
    </div>
  </Provider>
);

export default App;
