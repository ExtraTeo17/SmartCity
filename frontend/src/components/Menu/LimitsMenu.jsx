/* eslint-disable no-restricted-globals */
import { connect } from "react-redux";
import React, { useEffect, useState } from "react";
import { dispatch } from "../../redux/store";
import { startSimulationDataUpdated } from "../../redux/core/actions";

import {
  PED_MIN,
  PED_MAX,
  CAR_MIN,
  CAR_MAX,
  BIKE_MIN,
  BIKE_MAX,
  TIME_BEFORE_TP_MIN,
  TIME_BEFORE_TP_MAX,
  TEST_ID_MIN,
  TEST_ID_MAX,
} from "../../constants/minMax";

import { ConfigState, StartState } from "../../redux/models/states";
import "../../styles/Menu.css";
import { setIfValidInt } from "../../utils/helpers";
import {
  D_PEDS_NUM,
  D_TEST_PED,
  D_CARS_NUM,
  D_TEST_CAR,
  D_BIKES_NUM,
  D_TEST_BIKE,
  D_TIME_BEFORE_TROUBLE,
} from "../../constants/defaults";

const LimitsMenu = props => {
  const {
    configState,
    startState,
    wasStarted,
    generatePedestrians,
    generateCars,
    generateBikes,
    generateTroublePoints,
    startSimulationData,
  } = props;
  const [pedLimit, setPedLimit] = useState(startSimulationData.pedLimit);
  const [testPedId, setTestPedId] = useState(D_TEST_PED);

  const [carsLimit, setCarsLimit] = useState(D_CARS_NUM);
  const [testCarId, setTestCarId] = useState(D_TEST_CAR);

  const [bikesLimit, setBikesLimit] = useState(D_BIKES_NUM);
  const [testBikeId, setTestBikeId] = useState(D_TEST_BIKE);

  const [timeBeforeTrouble, setTimeBeforeTrouble] = useState(D_TIME_BEFORE_TROUBLE);

  function onStart() {
    if (startState === StartState.Invoke) {
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
  useEffect(onStart, [startState]);

  function onReplace() {
    if (configState !== ConfigState.Initial) {
      setPedLimit(startSimulationData.pedLimit);
      setTestPedId(startSimulationData.testPedId);

      setCarsLimit(startSimulationData.carsLimit);
      setTestCarId(startSimulationData.testCarId);

      setBikesLimit(startSimulationData.bikesLimit);
      setTestBikeId(startSimulationData.testBikeId);

      setTimeBeforeTrouble(startSimulationData.timeBeforeTrouble);
    }
  }
  useEffect(onReplace, [configState]);

  function evSetPedsLimit(e) {
    setIfValidInt(e, PED_MIN, PED_MAX, val => dispatch(startSimulationDataUpdated({ pedLimit: val })));
  }

  function evSetTestPedId(e) {
    setIfValidInt(e, TEST_ID_MIN, TEST_ID_MAX, val => dispatch(startSimulationDataUpdated({ testPedId: val })));
  }

  function evSetCarsLimit(e) {
    setIfValidInt(e, CAR_MIN, CAR_MAX, setCarsLimit);
  }

  function evSetTestCarId(e) {
    setIfValidInt(e, TEST_ID_MIN, TEST_ID_MAX, setTestCarId);
  }

  function evSetBikesLimit(e) {
    setIfValidInt(e, BIKE_MIN, BIKE_MAX, setBikesLimit);
  }

  function evSetTestBikeId(e) {
    setIfValidInt(e, TEST_ID_MIN, TEST_ID_MAX, setTestBikeId);
  }

  function evSetTimeBeforeTrouble(e) {
    setIfValidInt(e, TIME_BEFORE_TP_MIN, TIME_BEFORE_TP_MAX, setTimeBeforeTrouble);
  }

  return (
    <div key={configState}>
      {generatePedestrians && (
        <div className="mb-4 form-border">
          <div className="form-row mt-2 align-items-end">
            <div className="form-group col-md-5">
              <label htmlFor="pedLimit">Pedestrians limit</label>
              <input
                type="number"
                className="form-control"
                id="pedLimit"
                disabled={wasStarted}
                min={PED_MIN}
                max={PED_MAX}
                defaultValue={startSimulationData.pedLimit}
                placeholder="Enter limit for pedestrians"
                onChange={evSetPedsLimit}
              />
            </div>
            <div className="form-group col-md-7">
              <label htmlFor="testPedId">Test pedestrian number</label>
              <input
                type="number"
                className="form-control"
                id="testPedId"
                disabled={wasStarted}
                min={TEST_ID_MIN}
                max={TEST_ID_MAX}
                defaultValue={startSimulationData.testPedId}
                placeholder="Enter test pedestrians number"
                onChange={evSetTestPedId}
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
                className="form-control"
                id="carsNum"
                disabled={wasStarted}
                min={CAR_MIN}
                max={CAR_MAX}
                defaultValue={startSimulationData.carsLimit}
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
                min={TEST_ID_MIN}
                max={TEST_ID_MAX}
                defaultValue={startSimulationData.testCarId}
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
                className="form-control"
                id="bikesNum"
                disabled={wasStarted}
                min={BIKE_MIN}
                max={BIKE_MAX}
                placeholder="Enter limit for bikes"
                defaultValue={startSimulationData.bikesLimit}
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
                min={TEST_ID_MIN}
                max={TEST_ID_MAX}
                defaultValue={startSimulationData.testBikeId}
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
                className="form-control"
                id="timeBeforeTrouble"
                disabled={wasStarted}
                min={TIME_BEFORE_TP_MIN}
                max={TIME_BEFORE_TP_MAX}
                defaultValue={startSimulationData.timeBeforeTrouble}
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
    </div>
  );
};

const mapStateToProps = (state /* ownProps */) => {
  const { wasStarted } = state.message;
  const {
    startSimulationData,
    startState,
    configState,
    prepareSimulationData: { generatePedestrians },
  } = state.interaction;

  return {
    configState,
    startState,
    wasStarted,
    startSimulationData,
    generatePedestrians,
    generateCars: startSimulationData.generateCars,
    generateBikes: startSimulationData.generateBikes,
    generateTroublePoints: startSimulationData.generateTroublePoints,
  };
};

export default connect(mapStateToProps)(React.memo(LimitsMenu));
