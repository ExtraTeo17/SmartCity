/* eslint-disable no-restricted-globals */
import { connect } from "react-redux";
import React from "react";
import ApiManager from "../../../web/ApiManager";
import { centerMenuUpdated, simulationPrepareStarted } from "../../../redux/core/actions";
import { dispatch } from "../../../redux/store";

import { D_DECIMAL_PLACES } from "../../../constants/defaults";
import { LAT_MIN, LAT_MAX, LNG_MIN, LNG_MAX, RAD_MIN, RAD_MAX } from "../../../constants/minMax";

import "../../../styles/Menu.css";
import { setIfValidFloat } from "../../../utils/helpers";

const PrepareMenu = props => {
  const { configState, inPreparation, wasPrepared, wasStarted, prepareSimulationData } = props;

  function setLat(e) {
    setIfValidFloat(e, LAT_MIN, LAT_MAX, val => dispatch(centerMenuUpdated({ lat: val })));
  }

  function setLng(e) {
    setIfValidFloat(e, LNG_MIN, LNG_MAX, val => dispatch(centerMenuUpdated({ lng: val })));
  }

  function setRad(e) {
    setIfValidFloat(e, RAD_MIN, RAD_MAX, val => dispatch(centerMenuUpdated({ rad: val })));
  }

  function prepareSimulation() {
    if (ApiManager.isConnected()) {
      dispatch(simulationPrepareStarted());
      ApiManager.prepareSimulation(prepareSimulationData);
    }
  }

  let {
    center: { lat, lng, rad },
  } = prepareSimulationData;
  lat = lat.toFixed(D_DECIMAL_PLACES);
  lng = lng.toFixed(D_DECIMAL_PLACES);

  return (
    <form className="mb-4 form-border">
      <div key={configState}>
        <div className="form-row">
          <div className="form-group col-md-6">
            <label htmlFor="lat">Latitude </label>
            <input
              type="number"
              defaultValue={lat}
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
              defaultValue={lng}
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
              defaultValue={rad}
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
      </div>

      <div className="center-wrapper mt-3">
        <button
          className="btn btn-primary"
          disabled={inPreparation || wasStarted}
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
  const { inPreparation, wasPrepared, wasStarted } = state.message;
  const { prepareSimulationData, configState } = state.interaction;
  return {
    configState,
    prepareSimulationData,
    inPreparation,
    wasPrepared,
    wasStarted,
  };
};

export default connect(mapStateToProps)(React.memo(PrepareMenu));
