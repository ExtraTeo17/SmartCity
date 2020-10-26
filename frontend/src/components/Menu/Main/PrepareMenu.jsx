/* eslint-disable no-restricted-globals */
import { connect } from "react-redux";
import React, { useState, useEffect } from "react";
import ApiManager from "../../../web/ApiManager";
import { centerUpdated, startSimulationDataUpdated } from "../../../redux/actions";
import { dispatch } from "../../../redux/store";
import { StartState } from "../../../redux/models/startState";

import "../../../styles/Menu.css";

const PED_MIN = 1;
const PED_MAX = 200;

const DECIMAL_PLACES = 5;
const latMin = -90;
const latMax = 90;
const lngMin = -180;
const lngMax = 180;
const radMin = 0;
const radMax = 10000;

const PrepareMenu = props => {
  const { wasPrepared, wasStarted, shouldStart } = props;
  const [generatePedestrians, setGeneratePedestrians] = useState(false);
  const [pedLimit, setPedLimit] = useState(20);
  const [testPedId, setTestPedId] = useState(5);

  const onStart = () => {
    if (shouldStart === StartState.Invoke) {
      dispatch(
        startSimulationDataUpdated({
          pedLimit,
          testPedId,
        })
      );
    }
  };
  useEffect(onStart, [shouldStart]);

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

  let {
    center: { lat, lng, rad },
  } = props;
  lat = lat.toFixed(DECIMAL_PLACES);
  lng = lng.toFixed(DECIMAL_PLACES);

  const prepareSimulation = () => {
    ApiManager.prepareSimulation({ lat, lng, rad, generatePedestrians });
  };

  return (
    <form className="mb-4 form-border">
      <div className="form-row">
        <div className="form-group col-md-6">
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

        <div className="form-group col-md-6">
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
      </div>
      <div className="form-group">
        <label htmlFor="rad">Radius</label>
        <div className="input-group">
          <input
            type="number"
            defaultValue={rad}
            className="form-control"
            id="rad"
            step="10"
            min={radMin}
            max={radMax}
            placeholder="Enter radius"
            onChange={e => setRad(parseFloat(e.target.value))}
          />
          <div className="input-group-append">
            <span className="input-group-text">meters</span>
          </div>
        </div>
      </div>

      <div className="form-check user-select-none">
        <input
          type="checkbox"
          defaultChecked={generatePedestrians}
          className="form-check-input"
          id="generatePedestrians"
          onChange={e => setGeneratePedestrians(e.target.checked)}
        />
        <label htmlFor="generatePedestrians" className="form-check-label">
          Generate pedestrians, buses and stations
        </label>
      </div>
      {generatePedestrians && (
        <div className="form-row mt-2 align-items-end">
          <div className="form-group col-md-5">
            <label htmlFor="pedLimit">Pedestrians limit</label>
            <input
              type="number"
              defaultValue={pedLimit}
              className="form-control"
              id="pedLimit"
              disabled={wasStarted}
              min={PED_MIN}
              max={PED_MAX}
              placeholder="Enter limit for pedestrians"
              onChange={e => setPedLimit(parseInt(e.target.value))}
            />
          </div>
          <div className="form-group col-md-7">
            <label htmlFor="testPedId">Test pedestrian number</label>
            <input
              type="number"
              className="form-control"
              id="testPedId"
              disabled={wasStarted}
              min={1}
              max={1000}
              defaultValue={testPedId}
              placeholder="Enter test pedestrians number"
              onChange={e => setTestPedId(parseInt(e.target.value))}
            />
          </div>
        </div>
      )}

      <div className="center-wrapper mt-3">
        <button
          className="btn btn-primary"
          disabled={wasStarted}
          title={wasStarted ? "Simulation already started!" : wasPrepared ? "Simulation already prepared!" : "Prepare simulation"}
          type="button"
          onClick={prepareSimulation}
        >
          Prepare simulation
        </button>
      </div>
    </form>
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

export default connect(mapStateToProps)(React.memo(PrepareMenu));
