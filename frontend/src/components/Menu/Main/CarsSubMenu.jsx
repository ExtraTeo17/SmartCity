/* eslint-disable no-restricted-globals */
import { connect } from "react-redux";
import React, { useEffect, useState } from "react";
import { dispatch } from "../../../redux/store";
import { startSimulationDataUpdated } from "../../../redux/core/actions";

import "../../../styles/Menu.css";
import { StartState } from "../../../redux/models/startState";

const CAR_MIN = 1;
const CAR_MAX = 50;
const DEFAULT_CARS_NUM = 4;
const DEFAULT_TEST_CAR = 2;

const CarsSubMenu = props => {
  const { shouldStart, wasStarted } = props;
  const [carsLimit, setCarsLimit] = useState(DEFAULT_CARS_NUM);
  const [testCarId, setTestCarId] = useState(DEFAULT_TEST_CAR);
  const [generateCars, setGenerateCars] = useState(true);
  const [generateTroublePoints, setGenerateTroublePoints] = useState(false);
  const [timeBeforeTrouble, setTimeBeforeTrouble] = useState(5);

  function onStart() {
    if (shouldStart === StartState.Invoke) {
      dispatch(startSimulationDataUpdated({ carsLimit, testCarId, generateCars, generateTroublePoints, timeBeforeTrouble }));
    }
  }
  useEffect(onStart, [shouldStart]);

  function evSetCarsLimit(e) {
    const val = parseInt(e.target.value);
    if (!isNaN(val)) {
      setCarsLimit(val);
    }
  }

  function evSetTestCarId(e) {
    const val = parseInt(e.target.value);
    if (!isNaN(val)) {
      setTestCarId(val);
    }
  }

  function evSetGenerateCars(e) {
    setGenerateCars(e.target.checked);
  }

  function evSetGenerateTroublePoints(e) {
    setGenerateTroublePoints(e.target.checked);
  }

  function evSetTimeBeforeTrouble(e) {
    const val = parseInt(e.target.value);
    if (!isNaN(val)) {
      setTimeBeforeTrouble(val);
    }
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
      {generateCars && (
        <div className="form-row  mt-2 align-items-end">
          <div className="form-group col-md-5">
            <label htmlFor="carsNum">Cars limit</label>
            <input
              type="number"
              defaultValue={carsLimit}
              className="form-control"
              id="carsNum"
              disabled={wasStarted}
              min={CAR_MIN}
              max={CAR_MAX}
              placeholder="Enter limit for cars"
              onChange={evSetCarsLimit}
            />
          </div>
          <div className="form-group col-md-7">
            <label htmlFor="testCarId">Test car number</label>
            <input
              type="number"
              className="form-control"
              id="testCarId"
              disabled={wasStarted}
              min={1}
              max={1000}
              defaultValue={testCarId}
              placeholder="Enter test car number"
              onChange={evSetTestCarId}
            />
          </div>
        </div>
      )}

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
              onChange={evSetTimeBeforeTrouble}
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

const mapStateToProps = (state /* ownProps */) => {
  const { wasPrepared, wasStarted } = state.message;
  const {
    startSimulationData: { carsNum, testCarNum, generateCars, generateTroublePoints, timeBeforeTrouble },
    shouldStart,
  } = state.interaction;
  return {
    wasPrepared,
    shouldStart,
    wasStarted,
    carsNum,
    testCarNum,
    generateCars,
    generateTroublePoints,
    timeBeforeTrouble,
  };
};

export default connect(mapStateToProps)(React.memo(CarsSubMenu));
