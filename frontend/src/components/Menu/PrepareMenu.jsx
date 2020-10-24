/* eslint-disable no-restricted-globals */
import { connect } from "react-redux";
import React, { useState } from "react";
import ApiManager from "../../web/ApiManager";
import { centerUpdated } from "../../redux/actions";
import { dispatch } from "../../redux/store";
import "../../styles/Menu.css";

const DECIMAL_PLACES = 5;
const latMin = -90;
const latMax = 90;
const lngMin = -180;
const lngMax = 180;
const radMin = 0;
const radMax = 10000;

const PrepareMenu = props => {
  const { wasPrepared, wasStarted } = props;
  let {
    center: { lat, lng, rad },
  } = props;
  lat = lat.toFixed(DECIMAL_PLACES);
  lng = lng.toFixed(DECIMAL_PLACES);

  const [generatePedestrians, setGeneratePedestrians] = useState(false);

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

  return (
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
      <div className="form-check user-select-none">
        <input
          type="checkbox"
          checked={generatePedestrians}
          className="form-check-input"
          id="generatePedestrians"
          onChange={e => setGeneratePedestrians(e.target.checked)}
        />
        <label htmlFor="generatePedestrians" className="form-check-label">
          Generate pedestrians, buses and stations
        </label>
      </div>
      <div className="center-wrapper mt-3">
        <button
          className="btn btn-primary"
          disabled={wasStarted}
          title={wasStarted ? "Simulation already started!" : wasPrepared ? "Simulation already prepared!" : "Prepare simulation"}
          type="button"
          onClick={() => ApiManager.prepareSimulation({ lat, lng, rad, generatePedestrians })}
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

export default connect(mapStateToProps)(PrepareMenu);
