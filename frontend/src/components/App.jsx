import React from "react";
import CityMap from "./CityMap";
import "../styles/App.css";
import Menu from "./Menu";
import Notifications from "react-notify-toast";

import { Provider } from "react-redux";
import store from "../redux/store";

const App = () => (
  <Provider store={store}>
    <div className="App">
      <Notifications />
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
