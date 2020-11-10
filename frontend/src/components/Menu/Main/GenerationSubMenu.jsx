/* eslint-disable no-restricted-globals */
import { connect } from "react-redux";
import React from "react";
import { dispatch } from "../../../redux/store";
import { startSimulationDataUpdated } from "../../../redux/core/actions";
import "../../../styles/Menu.css";

const GenerationSubMenu = props => {
  const { wasStarted, generateCars, generateBikes, generateTroublePoints } = props;

  function dispatchUpdate(data) {
    dispatch(startSimulationDataUpdated(data));
  }

  function evSetGenerateCars(e) {
    dispatchUpdate({ generateCars: e.target.checked });
  }

  function evSetGenerateBikes(e) {
    dispatchUpdate({ generateBikes: e.target.checked });
  }

  function evSetGenerateTroublePoints(e) {
    dispatchUpdate({ generateTroublePoints: e.target.checked });
  }

  return (
    <>
      <div className="form-check user-select-none">
        <input
          type="checkbox"
          disabled={wasStarted}
          checked={generateCars}
          className="form-check-input"
          id="generateCars"
          onChange={evSetGenerateCars}
        />
        <label htmlFor="generateCars" className="form-check-label">
          Generate cars
        </label>
      </div>

      <div className="form-check user-select-none">
        <input
          type="checkbox"
          disabled={wasStarted}
          checked={generateBikes}
          className="form-check-input"
          id="generateBikes"
          onChange={evSetGenerateBikes}
        />
        <label htmlFor="generateBikes" className="form-check-label">
          Generate bikes
        </label>
      </div>

      <div className="form-check user-select-none">
        <input
          type="checkbox"
          defaultChecked={generateTroublePoints}
          disabled={!generateCars || wasStarted}
          className="form-check-input"
          id="generateTroublePoints"
          onChange={evSetGenerateTroublePoints}
        />
        <label htmlFor="generateTroublePoints" className="form-check-label">
          Generate trouble points
        </label>
      </div>
    </>
  );
};

const mapStateToProps = (state /* ownProps */) => {
  const { wasStarted } = state.message;
  const {
    startSimulationData: { generateCars, generateBikes, generateTroublePoints },
  } = state.interaction;
  return {
    wasStarted,
    generateCars,
    generateBikes,
    generateTroublePoints,
  };
};

export default connect(mapStateToProps)(React.memo(GenerationSubMenu));
