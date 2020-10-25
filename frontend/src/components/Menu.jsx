import ApiManager from "../web/ApiManager";
import { centerUpdated } from "../redux/actions";
import { dispatch } from "../redux/store";
import { connect } from "react-redux";
import React, { useState } from "react";
import "../styles/Menu.css";

const DEFAULT_CARS_NUM = 4;
const DEFAULT_TEST_CAR = 2;

const DECIMAL_PLACES = 5;

const Menu = props => {
  const { wasPrepared, wasStarted } = props;
  let { lat, lng } = props.center;
  lat = lat.toFixed(DECIMAL_PLACES);
  lng = lng.toFixed(DECIMAL_PLACES);
  const rad = props.center.rad;

  const [carsNum, setCarsNum] = useState(DEFAULT_CARS_NUM);
  const [testCarNum, setTestCarNum] = useState(DEFAULT_TEST_CAR);

  const latMin = -90,
    latMax = 90;
  const lngMin = -180,
    lngMax = 180;
  const radMin = 0,
    radMax = 10000;
  const carMin = 1,
    carMax = 50;

  /**
   * @param {number} val
   */
  const setLat = val => {
    if (!isNaN(val) && val >= latMin && val <= latMax) {
      const center = { ...props.center, lat: val };
      dispatch(centerUpdated(center));
    }
  };

  /**
   * @param {number} val
   */
  const setLng = val => {
    if (!isNaN(val) && val >= lngMin && val <= lngMax) {
      const center = { ...props.center, lng: val };
      dispatch(centerUpdated(center));
    }
  };

  /**
   * @param {number} val
   */
  const setRad = val => {
    if (!isNaN(val) && val >= radMin && val <= radMax) {
      const center = { ...props.center, rad: val };
      dispatch(centerUpdated(center));
    }
  };

  return (
    <div>
      <form className="mb-4 form-border">
        <div className="form-group">
          <label htmlFor="lat">Latitude</label>
          <input
            type="number"
            value={lat}
            className="form-control"
            id="lat"
            step="0.0001"
            min={latMin}
            max={latMax}
            placeholder="Enter latitude"
            onChange={e => setLat(parseFloat(e.target.value))}
          />
        </div>
        <div className="form-group">
          <label htmlFor="lng">Longitude</label>
          <input
            type="number"
            value={lng}
            className="form-control"
            id="lng"
            step="0.0001"
            min={lngMin}
            max={lngMax}
            placeholder="Enter longitude"
            onChange={e => setLng(parseFloat(e.target.value))}
          />
        </div>
        <div className="form-group">
          <label htmlFor="rad">Radius</label>
          <input
            type="number"
            value={rad}
            className="form-control"
            id="rad"
            step="10"
            min={radMin}
            max={radMax}
            placeholder="Enter radius"
            onChange={e => setRad(parseFloat(e.target.value))}
          />
        </div>
        <button
          className="btn btn-primary"
          disabled={wasPrepared || wasStarted}
          title={wasStarted ? "Simulation already started!" : wasPrepared ? "Simulation already prepared!" : "Prepare simulation"}
          type="button"
          onClick={() => ApiManager.prepareSimulation({ lat, lng, rad })}
        >
          Prepare simulation
        </button>
      </form>
      <form className="form-border">
        <div className="form-group">
          <label htmlFor="carsNum">Cars limit</label>
          <input
            type="number"
            defaultValue={carsNum}
            className="form-control"
            id="carsNum"
            min={carMin}
            max={carMax}
            placeholder="Enter limit for cars"
            onChange={e => setCarsNum(parseInt(e.target.value))}
          />
        </div>
        <div className="form-group">
          <label htmlFor="testCarNum">Test car number</label>
          <input
            type="number"
            defaultValue={testCarNum}
            className="form-control"
            id="testCarNum"
            min={carMin}
            max={carsNum}
            placeholder="Enter test car number"
            onChange={e => setTestCarNum(parseInt(e.target.value))}
          />
        </div>
        <button
          className="btn btn-success"
          disabled={wasStarted || !wasPrepared}
          title={
            wasStarted
              ? "Simulation already started"
              : wasPrepared
              ? "Simulation is ready to run"
              : "You must prepare simulation!"
          }
          type="button"
          onClick={() => ApiManager.startVehicles({ carsNum, testCarNum })}
        >
          Start simulation
        </button>
      </form>
    </div>
  );
};

const mapStateToProps = (state /* , ownProps */) => {
  var { wasPrepared, wasStarted } = state.message;
  return {
    center: state.interaction.center,
    wasPrepared: wasPrepared,
    wasStarted: wasStarted,
  };
};

export default connect(mapStateToProps)(Menu);
