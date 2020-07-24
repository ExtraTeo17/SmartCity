import React from "react";
import SplitPane, { Pane } from "react-split-pane";
import CityMap from "./CityMap";
import ApiManager from "../web/ApiManager";
import "../styles/App.css";

class App extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      apiManager: new ApiManager(),
    };
  }

  componentDidMount() {}

  render() {
    return (
      <div className="App">
        <header className="App-header">
          <SplitPane split="vertical" minSize={400} defaultSize={1200} primary="first">
            <Pane>
              <CityMap />
            </Pane>
            <Pane>
              <div className="row justify-content-center">
                <form>
                  <div className="form-group">
                    <label htmlFor="lat">Latitude</label>
                    <input
                      type="number"
                      defaultValue={52.217154}
                      className="form-control"
                      id="lat"
                      placeholder="Enter latitude"
                    />
                  </div>
                  <div className="form-group">
                    <label htmlFor="long">Longitude</label>
                    <input
                      type="number"
                      defaultValue={21.005575}
                      className="form-control"
                      id="long"
                      placeholder="Enter longitude"
                    />
                  </div>
                  <button className="btn btn-primary" type="button">
                    {" "}
                    Set zone{" "}
                  </button>
                </form>
              </div>
            </Pane>
          </SplitPane>
        </header>
      </div>
    );
  }
}

export default App;
