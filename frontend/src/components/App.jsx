import React from "react";
import SplitPane, { Pane } from "react-split-pane";
import CityMap from "./CityMap";
import ApiManager from "../web/ApiManager";
import "../styles/App.css";

/*TODO: Functional component? */
class App extends React.Component {
  constructor(props) {
    super(props);
    // TODO: connect to Map
    this.state = {
      apiManager: new ApiManager(),
      latitude: 52.23682,
      longitude: 21.01681,
      radius: 200,
    };

    this.setLatitude = this.setLatitude.bind(this);
    this.setLongtitude = this.setLongtitude.bind(this);
    this.setRadius = this.setRadius.bind(this);
  }

  setLatitude(val) {
    this.setState({ latitude: val });
  }

  setLongtitude(val) {
    this.setState({ longitude: val });
  }

  setRadius(val) {
    this.setState({ radius: val });
  }

  componentDidMount() {}

  render() {
    const { apiManager, latitude, longitude, radius } = this.state;

    return (
      <div className="App">
        <header className="App-header">
          <SplitPane split="vertical" minSize={400} defaultSize={1200} primary="first">
            <Pane>
              <CityMap />
            </Pane>
            {/* TODO: Reload part of map on pane move */}
            <Pane>
              {/* TODO: Move to separate component */}
              <div className="row justify-content-center">
                <form>
                  <div className="form-group">
                    <label htmlFor="lat">Latitude</label>
                    <input
                      type="number"
                      defaultValue={latitude}
                      className="form-control"
                      id="lat"
                      placeholder="Enter latitude"
                      onChange={e => this.setLatitude(e.target.value)}
                    />
                  </div>
                  <div className="form-group">
                    <label htmlFor="long">Longitude</label>
                    <input
                      type="number"
                      defaultValue={longitude}
                      className="form-control"
                      id="long"
                      placeholder="Enter longitude"
                      onChange={e => this.setLongtitude(e.target.value)}
                    />
                  </div>
                  <div className="form-group">
                    <label htmlFor="rad">Radius</label>
                    <input
                      type="number"
                      defaultValue={radius}
                      className="form-control"
                      id="rad"
                      placeholder="Enter radius"
                      onChange={e => this.setRadius(e.target.value)}
                    />
                  </div>
                  <button
                    className="btn btn-primary"
                    type="button"
                    onClick={() => apiManager.setZone({ latitude, longitude, radius })}
                  >
                    Set zone
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
