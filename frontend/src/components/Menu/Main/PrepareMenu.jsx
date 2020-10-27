/* eslint-disable no-restricted-globals */
import { connect } from "react-redux";
import React from "react";
import ApiManager from "../../../web/ApiManager";
import { centerUpdated, generatePedestriansUpdated } from "../../../redux/core/actions";
import { dispatch } from "../../../redux/store";

import { D_DECIMAL_PLACES } from "../../../constants/defaults";
import { LAT_MIN, LAT_MAX, LNG_MIN, LNG_MAX, RAD_MIN, RAD_MAX } from "../../../constants/minMax";

import "../../../styles/Menu.css";

const PrepareMenu = props => {
  const { wasPrepared, wasStarted, generatePedestrians } = props;

  const setLat = e => {
    const val = parseFloat(e.target.value);
    if (!isNaN(val) && val >= LAT_MIN && val <= LAT_MAX) {
      dispatch(centerUpdated({ lat: val }));
    }
  };

  const setLng = e => {
    const val = parseFloat(e.target.value);
    if (!isNaN(val) && val >= LNG_MIN && val <= LNG_MAX) {
      dispatch(centerUpdated({ lng: val }));
    }
  };

  const setRad = e => {
    const val = parseFloat(e.target.value);
    if (!isNaN(val) && val >= RAD_MIN && val <= RAD_MAX) {
      dispatch(centerUpdated({ rad: val }));
    }
  };

  const setGeneratePedestrians = e => {
    const val = e.target.checked;
    dispatch(generatePedestriansUpdated(val));
  };

  let {
    center: { lat, lng, rad },
  } = props;
  lat = lat.toFixed(D_DECIMAL_PLACES);
  lng = lng.toFixed(D_DECIMAL_PLACES);

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
            min={LAT_MIN}
            max={LAT_MAX}
            placeholder="Enter latitude"
            onChange={setLat}
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
            min={LNG_MIN}
            max={LNG_MAX}
            placeholder="Enter longitude"
            onChange={setLng}
          />
        </div>
      </div>
      <div className="form-group">
        <label htmlFor="rad">Radius</label>
        <div className="input-group">
          <input
            type="number"
            value={rad}
            className="form-control"
            id="rad"
            step="10"
            min={RAD_MIN}
            max={RAD_MAX}
            placeholder="Enter radius"
            onChange={setRad}
          />
          <div className="input-group-append">
            <span className="input-group-text">meters</span>
          </div>
        </div>
      </div>

      <div className="form-check user-select-none">
        <input
          type="checkbox"
          checked={generatePedestrians}
          className="form-check-input"
          id="generatePedestrians"
          onChange={setGeneratePedestrians}
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
  const { center, generatePedestrians } = state.interaction;
  return {
    center,
    wasPrepared,
    wasStarted,
    generatePedestrians,
  };
};

export default connect(mapStateToProps)(React.memo(PrepareMenu));
