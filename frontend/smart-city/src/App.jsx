import React from "react";
import { SERVER_ADDRESS } from "./utils/constants";
import SmartCityMap from "./Map";

import "./styles/App.css";

class App extends React.Component {
  constructor(props) {
    super(props);
  }

  componentDidMount() {
    let socket = new WebSocket(SERVER_ADDRESS);
    socket.onopen = e => {
      console.log("Connected !!!");
    };
  }

  render() {
    return (
      <div className="App">
        <header className="App-header">
          <SmartCityMap />
        </header>
      </div>
    );
  }
}

export default App;
