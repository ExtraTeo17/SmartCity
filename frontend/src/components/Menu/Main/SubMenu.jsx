/* eslint-disable no-restricted-globals */
import { connect } from "react-redux";
import React, { useEffect, useState } from "react";
import { dispatch } from "../../../redux/store";
import { startSimulationDataUpdated } from "../../../redux/core/actions";

import {
  D_PEDS_NUM,
  D_TEST_PED,
  D_GENERATE_CARS,
  D_CARS_NUM,
  D_TEST_CAR,
  D_GENERATE_BIKES,
  D_BIKES_NUM,
  D_TEST_BIKE,
  D_GENERATE_TP,
  D_TIME_BEFORE_TROUBLE,
  D_GENERATE_TJ,
} from "../../../constants/defaults";

import {
  PED_MIN,
  PED_MAX,
  CAR_MIN,
  CAR_MAX,
  BIKE_MIN,
  BIKE_MAX,
  TIME_BEFORE_TP_MIN,
  TIME_BEFORE_TP_MAX,
} from "../../../constants/minMax";

import { StartState } from "../../../redux/models/startState";
import "../../../styles/Menu.css";
import { setIfValidInt } from "../../../utils/helpers";

const SubMenu = props => {
  const { shouldStart, wasStarted, generatePedestrians } = props;
  const [pedLimit, setPedLimit] = useState(D_PEDS_NUM);
  const [testPedId, setTestPedId] = useState(D_TEST_PED);

  const [generateCars, setGenerateCars] = useState(D_GENERATE_CARS);
  const [carsLimit, setCarsLimit] = useState(D_CARS_NUM);
  const [testCarId, setTestCarId] = useState(D_TEST_CAR);

  const [generateBikes, setGenerateBikes] = useState(D_GENERATE_BIKES);
  const [bikesLimit, setBikesLimit] = useState(D_BIKES_NUM);
  const [testBikeId, setTestBikeId] = useState(D_TEST_BIKE);

  const [generateTroublePoints, setGenerateTroublePoints] = useState(D_GENERATE_TP);
  const [generateTrafficJams, setGenerateTrafficJams] = useState(D_GENERATE_TJ);
  const [timeBeforeTrouble, setTimeBeforeTrouble] = useState(D_TIME_BEFORE_TROUBLE);

  function onStart() {
    if (shouldStart === StartState.Invoke) {
      dispatch(
        startSimulationDataUpdated({
          generateCars,
          carsLimit,
          testCarId,

          generateBikes,
          bikesLimit,
          testBikeId,

          generateTroublePoints,
          generateTrafficJams,
          timeBeforeTrouble,
          pedLimit,
          testPedId,
        })
      );
    }
  }
  useEffect(onStart, [shouldStart]);

  function evSetGenerateCars(e) {
    setGenerateCars(e.target.checked);
  }

  function evSetCarsLimit(e) {
    setIfValidInt(e, CAR_MIN, CAR_MAX, setCarsLimit);
  }

  function evSetTestCarId(e) {
    setIfValidInt(e, 1, 1000, setTestCarId);
  }

  function evSetGenerateBikes(e) {
    setGenerateBikes(e.target.checked);
  }

  function evSetBikesLimit(e) {
    setIfValidInt(e, BIKE_MIN, BIKE_MAX, setBikesLimit);
  }

  function evSetTestBikeId(e) {
    setIfValidInt(e, 1, 1000, setTestBikeId);
  }

  function evSetGenerateTroublePoints(e) {
    setGenerateTroublePoints(e.target.checked);
  }

  function evSetTimeBeforeTrouble(e) {
    setIfValidInt(e, TIME_BEFORE_TP_MIN, TIME_BEFORE_TP_MAX, setTimeBeforeTrouble);
  }

  function evSetGenerateTrafficJams(e) {
    setGenerateTrafficJams(e.target.checked);
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
      {generateBikes && (
        <div className="form-row  mt-2 align-items-end">
          <div className="form-group col-md-5">
            <label htmlFor="carsNum">Bikes limit</label>
            <input
              type="number"
              defaultValue={bikesLimit}
              className="form-control"
              id="bikesNum"
              disabled={wasStarted}
              min={BIKE_MIN}
              max={BIKE_MAX}
              placeholder="Enter limit for bikes"
              onChange={evSetBikesLimit}
            />
          </div>
          <div className="form-group col-md-7">
            <label htmlFor="testBikeId">Test bike number</label>
            <input
              type="number"
              className="form-control"
              id="testBikeId"
              disabled={wasStarted}
              min={1}
              max={1000}
              defaultValue={testBikeId}
              placeholder="Enter test bike number"
              onChange={evSetTestBikeId}
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
              min={TIME_BEFORE_TP_MIN}
              max={TIME_BEFORE_TP_MAX}
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
      <div className="form-check user-select-none">
        <input
          type="checkbox"
          defaultChecked={generateTrafficJams}
          disabled={wasStarted}
          className="form-check-input"
          id="generateTrafficJams"
          onChange={evSetGenerateTrafficJams}
        />
        <label htmlFor="generateTrafficJams" className="form-check-label">
          Generate traffic jams
        </label>
      </div>
    </>
  );
};

const mapStateToProps = (state /* ownProps */) => {
  const { wasPrepared, wasStarted } = state.message;
  const {
    startSimulationData: { carsNum, testCarNum, generateCars, generateTrafficJams, generateTroublePoints, timeBeforeTrouble },
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
    generateTrafficJams,
    generateTroublePoints,
    timeBeforeTrouble,
  };
};

export default connect(mapStateToProps)(React.memo(SubMenu));
