/* eslint-disable no-restricted-globals */
/* eslint-disable indent */
import { connect } from "react-redux";
import React from "react";
import { dispatch } from "../../../redux/store";
import { startSimulationDataUpdated } from "../../../redux/actions";

import "../../../styles/Menu.css";

const CAR_MIN = 1;
const CAR_MAX = 50;

const CarsMenu = ({ wasStarted, carsNum, testCarNum, generateCars, generateTroublePoints, timeBeforeTrouble }) => {
  function setCarsNum(e) {
    if (e.target.value !== carsNum) {
      dispatch(startSimulationDataUpdated({ carsNum: e.target.value }));
    }
  }

  function setTestCarNum(e) {
    dispatch(startSimulationDataUpdated({ testCarNum: e.target.value }));
  }

  function setGenerateCars(e) {
    dispatch(startSimulationDataUpdated({ generateCars: e.target.checked }));
  }

  function setGenerateTroublePoints(e) {
    dispatch(startSimulationDataUpdated({ generateTroublePoints: e.target.checked }));
  }

  function setTimeBeforeTrouble(e) {
    dispatch(startSimulationDataUpdated({ timeBeforeTrouble: e.target.value }));
  }

  return (
    <>
      <div className="form-row">
        <div className="form-group col-md-6">
          <label htmlFor="carsNum">Cars limit</label>
          <input
            type="number"
            defaultValue={carsNum}
            className="form-control"
            id="carsNum"
            disabled={!generateCars || wasStarted}
            min={CAR_MIN}
            max={CAR_MAX}
            placeholder="Enter limit for cars"
            onChange={setCarsNum}
          />
        </div>
        <div className="form-grou col-md-6">
          <label htmlFor="testCarNum">Test car number</label>
          <input
            type="number"
            className="form-control"
            id="testCarNum"
            disabled={!generateCars || wasStarted}
            min={1}
            max={1000}
            defaultValue={testCarNum}
            placeholder="Enter test car number"
            onChange={setTestCarNum}
          />
        </div>
      </div>
      <div className="form-check user-select-none">
        <input type="checkbox" checked={generateCars} className="form-check-input" id="generateCars" onChange={setGenerateCars} />
        <label htmlFor="generateCars" className="form-check-label">
          Generate cars
        </label>
      </div>

      <div className="form-check user-select-none">
        <input
          type="checkbox"
          defaultChecked={generateTroublePoints}
          disabled={!generateCars || wasStarted}
          className="form-check-input"
          id="generateTroublePoints"
          onChange={setGenerateTroublePoints}
        />
        <label htmlFor="generateTroublePoints" className="form-check-label">
          Generate trouble points
        </label>
      </div>
      {generateTroublePoints && (
        <div className="form-group mt-2">
          <label htmlFor="timeBeforeTrouble">Time before road trouble occurs</label>
          <div className="input-group">
            <input
              type="number"
              defaultValue={timeBeforeTrouble}
              disabled={wasStarted}
              className="form-control"
              id="timeBeforeTrouble"
              placeholder="Enter time before road trouble occurs"
              onChange={setTimeBeforeTrouble}
            />
            <div className="input-group-append">
              <span className="input-group-text">seconds</span>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

const mapStateToProps = (state /* , ownProps */) => {
  const { wasPrepared, wasStarted } = state.message;
  const { carsNum, testCarNum, generateCars, generateTroublePoints, timeBeforeTrouble } = state.interaction.startSimulationData;
  return {
    wasPrepared,
    wasStarted,
    carsNum,
    testCarNum,
    generateCars,
    generateTroublePoints,
    timeBeforeTrouble,
  };
};

export default connect(mapStateToProps)(React.memo(CarsMenu));
