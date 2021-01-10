/* eslint-disable no-restricted-globals */
import { connect } from "react-redux";
import React from "react";
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

import "../../styles/Menu.css";
import { setIfValidInt } from "../../utils/helpers";

/**
 * Menu tab, which holds all limits for generated objects.
 * For cars, bike and pedestrians:
 *  - limit - number of generated objects at the same time, generation starts again when object disappear
 *  - test-object-id - number of generated test object, can be > limit
 *
 * For trouble points:
 *  - time before trouble - time of car ride (in simulation time) before road trouble occurs
 * @category Menu
 * @module LimitsMenu
 */
const LimitsMenu = props => {
  const {
    configState,
    wasStarted,
    generatePedestrians,
    generateCars,
    generateBikes,
    generateTroublePoints,
    startSimulationData: {
      pedLimit,
      testPedId,

      carsLimit,
      testCarId,

      bikesLimit,
      testBikeId,

      timeBeforeTrouble,
    },
  } = props;

  function evSetPedsLimit(e) {
    setIfValidInt(e, PED_MIN, PED_MAX, val => dispatch(startSimulationDataUpdated({ pedLimit: val })));
  }

  function evSetTestPedId(e) {
    setIfValidInt(e, TEST_ID_MIN, TEST_ID_MAX, val => dispatch(startSimulationDataUpdated({ testPedId: val })));
  }

  function evSetCarsLimit(e) {
    setIfValidInt(e, CAR_MIN, CAR_MAX, val => dispatch(startSimulationDataUpdated({ carsLimit: val })));
  }

  function evSetTestCarId(e) {
    setIfValidInt(e, TEST_ID_MIN, TEST_ID_MAX, val => dispatch(startSimulationDataUpdated({ testCarId: val })));
  }

  function evSetBikesLimit(e) {
    setIfValidInt(e, BIKE_MIN, BIKE_MAX, val => dispatch(startSimulationDataUpdated({ bikesLimit: val })));
  }

  function evSetTestBikeId(e) {
    setIfValidInt(e, TEST_ID_MIN, TEST_ID_MAX, val => dispatch(startSimulationDataUpdated({ testBikeId: val })));
  }

  function evSetTimeBeforeTrouble(e) {
    setIfValidInt(e, TIME_BEFORE_TP_MIN, TIME_BEFORE_TP_MAX, val =>
      dispatch(startSimulationDataUpdated({ timeBeforeTrouble: val }))
    );
  }

  return (
    <div key={configState}>
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
                defaultValue={carsLimit}
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
                defaultValue={testCarId}
                placeholder="Enter test car number"
                onChange={evSetTestCarId}
              />
            </div>
          </div>
        </div>
      )}

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
                defaultValue={pedLimit}
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
                defaultValue={testPedId}
                placeholder="Enter test pedestrians number"
                onChange={evSetTestPedId}
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
                defaultValue={bikesLimit}
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
                defaultValue={testBikeId}
                placeholder="Enter test bike number"
                onChange={evSetTestBikeId}
              />
            </div>
          </div>
        </div>
      )}

      {generateCars && generateTroublePoints && (
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
                defaultValue={timeBeforeTrouble}
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
    configState,
    prepareSimulationData: { generatePedestrians },
  } = state.interaction;

  return {
    configState,
    wasStarted,
    startSimulationData,
    generatePedestrians,
    generateCars: startSimulationData.generateCars,
    generateBikes: startSimulationData.generateBikes,
    generateTroublePoints: startSimulationData.generateTroublePoints,
  };
};

export default connect(mapStateToProps)(React.memo(LimitsMenu));
