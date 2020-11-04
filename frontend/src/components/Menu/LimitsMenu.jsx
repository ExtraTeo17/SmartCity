/* eslint-disable no-restricted-globals */
import { connect } from "react-redux";
import React, { useEffect, useState } from "react";
import { dispatch } from "../../redux/store";
import { startSimulationDataUpdated } from "../../redux/core/actions";

import {
  D_PEDS_NUM,
  D_TEST_PED,
  D_CARS_NUM,
  D_TEST_CAR,
  D_BIKES_NUM,
  D_TEST_BIKE,
  D_TIME_BEFORE_TROUBLE,
} from "../../constants/defaults";

import {
  PED_MIN,
  PED_MAX,
  CAR_MIN,
  CAR_MAX,
  BIKE_MIN,
  BIKE_MAX,
  TIME_BEFORE_TP_MIN,
  TIME_BEFORE_TP_MAX,
} from "../../constants/minMax";

import { StartState } from "../../redux/models/startState";
import "../../styles/Menu.css";
import { setIfValidInt } from "../../utils/helpers";

const LimitsMenu = props => {
  const { shouldStart, wasStarted, generatePedestrians, generateCars, generateBikes, generateTroublePoints } = props;
  const [pedLimit, setPedLimit] = useState(D_PEDS_NUM);
  const [testPedId, setTestPedId] = useState(D_TEST_PED);

  const [carsLimit, setCarsLimit] = useState(D_CARS_NUM);
  const [testCarId, setTestCarId] = useState(D_TEST_CAR);

  const [bikesLimit, setBikesLimit] = useState(D_BIKES_NUM);
  const [testBikeId, setTestBikeId] = useState(D_TEST_BIKE);

  const [timeBeforeTrouble, setTimeBeforeTrouble] = useState(D_TIME_BEFORE_TROUBLE);

  function onStart() {
    if (shouldStart === StartState.Invoke) {
      dispatch(
        startSimulationDataUpdated({
          carsLimit,
          testCarId,

          bikesLimit,
          testBikeId,

          timeBeforeTrouble,
          pedLimit,
          testPedId,
        })
      );
    }
  }
  useEffect(onStart, [shouldStart]);

  function evSetCarsLimit(e) {
    setIfValidInt(e, CAR_MIN, CAR_MAX, setCarsLimit);
  }

  function evSetTestCarId(e) {
    setIfValidInt(e, 1, 1000, setTestCarId);
  }

  function evSetBikesLimit(e) {
    setIfValidInt(e, BIKE_MIN, BIKE_MAX, setBikesLimit);
  }

  function evSetTestBikeId(e) {
    setIfValidInt(e, 1, 1000, setTestBikeId);
  }

  function evSetTimeBeforeTrouble(e) {
    setIfValidInt(e, TIME_BEFORE_TP_MIN, TIME_BEFORE_TP_MAX, setTimeBeforeTrouble);
  }

  return (
    <>
      {generatePedestrians && (
        <div className="mb-4 form-border">
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
        </div>
      )}

      {generateCars && (
        <div className="mb-4 form-border">
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
        </div>
      )}

      {generateBikes && (
        <div className="mb-4 form-border">
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
        </div>
      )}

      {generateTroublePoints && (
        <div className="mb-4 form-border">
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
        </div>
      )}
    </>
  );
};

const mapStateToProps = (state /* ownProps */) => {
  const { wasStarted } = state.message;
  const {
    startSimulationData: { generateCars, generateBikes, generateTroublePoints },
    shouldStart,
    generatePedestrians,
  } = state.interaction;
  return {
    shouldStart,
    wasStarted,
    generatePedestrians,
    generateCars,
    generateBikes,
    generateTroublePoints,
  };
};

export default connect(mapStateToProps)(React.memo(LimitsMenu));
