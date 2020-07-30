import React from "react";
import CityMap from "./CityMap";
import "../styles/App.css";
import Menu from "./Menu";

/*TODO: Functional component? */
class App extends React.Component {
  constructor(props) {
    super(props);
    // TODO: connect to Map
    this.state = {
      latitude: 52.23682,
      longitude: 21.01681,
      radius: 200,
    };
  }

  componentDidMount() {}

  render() {
    return (
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
    );
  }
}

export default App;
