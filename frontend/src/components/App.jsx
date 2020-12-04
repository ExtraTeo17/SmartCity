import React from "react";
import { Provider } from "react-redux";
import Notifications from "react-notify-toast";

import CityMap from "./CityMap";
import store from "../redux/store";
import MenusContainer from "./Menu/MenusContainer";

import "../styles/App.css";

const App = () => (
  <Provider store={store}>
    <Notifications />
    <div className="App">
      <header className="App-header">
        <div className="row w-100 main-container">
          <div className="col" onContextMenu={e => e.preventDefault()}>
            <CityMap />
          </div>
          <div className="col-3 menu-col">
            <MenusContainer />
          </div>
        </div>
      </header>
    </div>
  </Provider>
);

export default App;
