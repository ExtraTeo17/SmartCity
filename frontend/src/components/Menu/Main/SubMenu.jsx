/* eslint-disable no-restricted-globals */
import { connect } from "react-redux";
import React, { useEffect, useState } from "react";
import { dispatch } from "../../../redux/store";
import { startSimulationDataUpdated } from "../../../redux/core/actions";

import {
  D_PEDS_NUM,
  D_TEST_PED,
  D_CARS_NUM,
  D_TEST_CAR,
  D_TIME_BEFORE_TROUBLE,
  D_GENERATE_CARS,
  D_GENERATE_TP,
} from "../../../constants/defaults";

import { PED_MIN, PED_MAX, CAR_MIN, CAR_MAX } from "../../../constants/minMax";

import { StartState } from "../../../redux/models/startState";
import "../../../styles/Menu.css";

const SubMenu = props => {
  const { shouldStart, wasStarted, generatePedestrians } = props;
  const [pedLimit, setPedLimit] = useState(D_PEDS_NUM);
  const [testPedId, setTestPedId] = useState(D_TEST_PED);
  const [carsLimit, setCarsLimit] = useState(D_CARS_NUM);
  const [testCarId, setTestCarId] = useState(D_TEST_CAR);
  const [generateCars, setGenerateCars] = useState(D_GENERATE_CARS);
  const [generateTroublePoints, setGenerateTroublePoints] = useState(D_GENERATE_TP);
  const [timeBeforeTrouble, setTimeBeforeTrouble] = useState(D_TIME_BEFORE_TROUBLE);

  function onStart() {
    if (shouldStart === StartState.Invoke) {
      dispatch(
        startSimulationDataUpdated({
          carsLimit,
          testCarId,
          generateCars,
          generateTroublePoints,
          timeBeforeTrouble,
          pedLimit,
          testPedId,
        })
      );
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
    generatePedestrians,
  } = state.interaction;
  return {
    wasPrepared,
    shouldStart,
    wasStarted,
    carsNum,
    testCarNum,
    generateCars,
    generatePedestrians,
    generateTroublePoints,
    timeBeforeTrouble,
  };
};

export default connect(mapStateToProps)(React.memo(SubMenu));
