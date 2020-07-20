import React from "react";
import * as Leaflet from "leaflet";
import "./App.css";
import { SERVER_ADDRESS } from "./constants";

class App extends React.Component {
  constructor(props) {
    super(props);
  }

  componentDidMount() {
    var map = Leaflet.map("mapid").setView([52.217154, 21.005575], 15);
    Leaflet.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
      maxZoom: 19,
      attribution: '&copy; <a href="https://openstreetmap.org/copyright">OpenStreetMap contributors</a>',
    }).addTo(map);

    var line = Leaflet.polyline([
      [52.217116, 21.014223],
      [52.217369, 21.006857],
      [52.217264, 21.005254],
      [52.220123, 21.005044],
      [52.219946, 21.011481],
      [52.219909, 21.011991],
      [52.219857, 21.012318],
      [52.218085, 21.015129],
      [52.217099, 21.014872],
    ]);
    map.addLayer(line);

    let socket = new WebSocket(SERVER_ADDRESS);
    socket.onopen = e => {
      console.log("Connected !!!");
    };
  }

  render() {
    return (
      <div className="App">
        <header className="App-header">
          <div id="mapid"></div>
        </header>
      </div>
    );
  }
}

export default App;
