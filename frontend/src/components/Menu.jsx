/* eslint-disable no-restricted-globals */
/* eslint-disable indent */
import { connect } from "react-redux";
import React, { useState } from "react";
import ApiManager from "../web/ApiManager";
import { centerUpdated } from "../redux/actions";
import { dispatch } from "../redux/store";
import "../styles/Menu.css";

const DEFAULT_CARS_NUM = 4;
const DEFAULT_TEST_CAR = 2;

const DECIMAL_PLACES = 5;

const Menu = props => {
  const { wasPrepared, wasStarted } = props;
  let {
    center: { lat, lng, rad },
  } = props;
  lat = lat.toFixed(DECIMAL_PLACES);
  lng = lng.toFixed(DECIMAL_PLACES);

  const [carsNum, setCarsNum] = useState(DEFAULT_CARS_NUM);
  const [testCarNum, setTestCarNum] = useState(DEFAULT_TEST_CAR);
  const [generateCars, setGenerateCars] = useState(true);
  const [generatePedestrians, setGeneratePedestrians] = useState(true);
  const [generateTroublePoints, setGenerateTroublePoints] = useState(true);

  const latMin = -90;
  const latMax = 90;
  const lngMin = -180;
  const lngMax = 180;
  const radMin = 0;
  const radMax = 10000;
  const carMin = 1;
  const carMax = 50;

  const setLat = val => {
    if (!isNaN(val) && val >= latMin && val <= latMax) {
      const center = { ...props.center, lat: val };
      dispatch(centerUpdated(center));
    }
  };

  const setLng = val => {
    if (!isNaN(val) && val >= lngMin && val <= lngMax) {
      const center = { ...props.center, lng: val };
      dispatch(centerUpdated(center));
    }
  };

  const setRad = val => {
    if (!isNaN(val) && val >= radMin && val <= radMax) {
      const center = { ...props.center, rad: val };
      dispatch(centerUpdated(center));
    }
  };

  const startSimulation = () => {
    ApiManager.startSimulation({
      carsNum,
      testCarNum,
      generateCars,
      generatePedestrians,
      generateTroublePoints: generateCars && generateTroublePoints,
    });
  };

  return (
    <div>
      <form className="mb-4 form-border">
        <div className="form-group">
          <label htmlFor="lat">Latitude </label>
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
        <div className="center-wrapper">
          <button
            className="btn btn-primary"
            disabled={wasStarted}
            title={
              wasStarted ? "Simulation already started!" : wasPrepared ? "Simulation already prepared!" : "Prepare simulation"
            }
            type="button"
            onClick={() => ApiManager.prepareSimulation({ lat, lng, rad })}
          >
            Prepare simulation
          </button>
        </div>
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
            onChange={e => setCarsNum(parseInt(e.target.value, 10))}
          />
        </div>
        <div className="form-group">
          <label htmlFor="testCarNum">Test car number</label>
          <input
            type="number"
            defaultValue={testCarNum}
            className="form-control"
            id="testCarNum"
            min={1}
            max={1000}
            placeholder="Enter test car number"
            onChange={e => setTestCarNum(parseInt(e.target.value, 10))}
          />
        </div>
        <div className="form-check">
          <input
            type="checkbox"
            checked={generateCars}
            className="form-check-input"
            id="generateCars"
            onChange={e => setGenerateCars(e.target.checked)}
          />
          <label htmlFor="generateCars" className="form-check-label">
            Generate cars
          </label>
        </div>
        <div className="form-check">
          <input
            type="checkbox"
            checked={generatePedestrians}
            className="form-check-input"
            id="generatePedestrians"
            onChange={e => setGeneratePedestrians(e.target.checked)}
          />
          <label htmlFor="generatePedestrians" className="form-check-label">
            Generate pedestrians
          </label>
        </div>
        <div className="form-check">
          <input
            type="checkbox"
            checked={generateTroublePoints}
            disabled={!generateCars}
            className="form-check-input"
            id="generateTroublePoints"
            onChange={e => setGenerateTroublePoints(e.target.checked)}
          />
          <label htmlFor="generateTroublePoints" className="form-check-label">
            Generate trouble points
          </label>
        </div>
        <div className="center-wrapper">
          <button
            className="btn btn-success mt-3"
            disabled={wasStarted || !wasPrepared}
            title={
              wasStarted
                ? "Simulation already started"
                : wasPrepared
                ? "Simulation is ready to run"
                : "You must prepare simulation!"
            }
            type="button"
            onClick={startSimulation}
          >
            Start simulation
          </button>
        </div>
      </form>
    </div>
  );
};

const mapStateToProps = (state /* , ownProps */) => {
  const { wasPrepared, wasStarted } = state.message;
  return {
    center: state.interaction.center,
    wasPrepared,
    wasStarted,
  };
};

export default connect(mapStateToProps)(Menu);
