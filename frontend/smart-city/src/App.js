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
