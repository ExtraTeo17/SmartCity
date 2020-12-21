/* eslint-disable no-restricted-globals */
import { connect } from "react-redux";
import React from "react";
import { dispatch } from "../../redux/store";
import { startSimulationDataUpdated, generatePedestriansUpdated } from "../../redux/core/actions";
import { setIfValidInt } from "../../utils/helpers";
import { LIGHT_EXTEND_MIN, LIGHT_EXTEND_MAX, STATION_EXTEND_MAX, STATION_EXTEND_MIN } from "../../constants/minMax";

import "../../styles/Menu.css";

const StrategyMenu = props => {
  const {
    configState,
    wasStarted,
    startSimulationData: {
      generateCars,
      generateBatchesForCars,
      generateBikes,

      generateTroublePoints,
      detectTrafficJams,
      generateBusFailures,

      lightStrategyActive,
      extendLightTime,

      stationStrategyActive,
      extendWaitTime,

      troublePointStrategyActive,
      trafficJamStrategyActive,
      transportChangeStrategyActive,
    },
    generatePedestrians,
  } = props;

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

  function evSetLightStrategyActive(e) {
    dispatchUpdate({ lightStrategyActive: e.target.checked });
  }

  function evSetLightExtend(e) {
    setIfValidInt(e, LIGHT_EXTEND_MIN, LIGHT_EXTEND_MAX, val => dispatchUpdate({ extendLightTime: val }));
  }

  function evSetStationStrategyActive(e) {
    dispatchUpdate({ stationStrategyActive: e.target.checked });
  }

  function evSetWaitExtend(e) {
    setIfValidInt(e, STATION_EXTEND_MIN, STATION_EXTEND_MAX, val => dispatchUpdate({ extendWaitTime: val }));
  }

  function evSetTroublePointStrategyActive(e) {
    dispatchUpdate({ troublePointStrategyActive: e.target.checked });
  }

  function evSetTrafficJamStrategyActive(e) {
	  console.log("-----------------------------------")
    dispatchUpdate({ trafficJamStrategyActive: e.target.checked });
  }

  function evSetTransportChangeStrategyActive(e) {
    dispatchUpdate({ transportChangeStrategyActive: e.target.checked });
  }

  function evSetDetectTrafficJams(e) {
    dispatchUpdate({ detectTrafficJams: e.target.checked });
  }

  function evSetGenerateBusFailures(e) {
    dispatchUpdate({ generateBusFailures: e.target.checked });
  }

  function evSetGenerateBatchesForCars() {
    dispatchUpdate({ generateBatchesForCars: !generateBatchesForCars });
  }

  function evSetGeneratePedestrians(e) {
    dispatch(generatePedestriansUpdated(e.target.checked));
  }

  return (
    <>
      <div className="mb-4 form-border">
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

        <div className="ml-4 mt-1 mb-1 small-text">
          <div className="custom-control custom-radio">
            <input
              type="radio"
              disabled={!generateCars}
              checked={!generateBatchesForCars}
              name="carsRadio"
              className="custom-control-input"
              id="carsRandom"
              onChange={evSetGenerateBatchesForCars}
            />
            <label className="custom-control-label" htmlFor="carsRandom">
              Random
            </label>
          </div>
          <div className="custom-control custom-radio">
            <input
              type="radio"
              disabled={!generateCars}
              checked={generateBatchesForCars}
              name="carsRadio"
              className="custom-control-input"
              id="carsBatched"
              onChange={evSetGenerateBatchesForCars}
            />
            <label className="custom-control-label" htmlFor="carsBatched">
              Batched
            </label>
          </div>
        </div>

        <div className="form-check user-select-none">
          <input
            type="checkbox"
            checked={generatePedestrians}
            className="form-check-input"
            id="generatePedestrians"
            onChange={evSetGeneratePedestrians}
          />
          <label htmlFor="generatePedestrians" className="form-check-label">
            Generate buses & stations & pedestrians
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
            checked={generateTroublePoints}
            disabled={!generateCars || wasStarted}
            className="form-check-input"
            id="generateTroublePoints"
            onChange={evSetGenerateTroublePoints}
          />
          <label
            htmlFor="generateTroublePoints"
            className="form-check-label"
            data-toggle="tooltip"
            data-placement="top"
            data-html="true"
            title="You need to generate <b>cars</b> to generate trouble points."
          >
            Generate trouble points
          </label>
        </div>

        <div className="form-check user-select-none">
          <input
            type="checkbox"
            checked={detectTrafficJams}
            disabled={!generateCars || wasStarted}
            className="form-check-input"
            id="detectTrafficJams"
            onChange={evSetDetectTrafficJams}
          />
          <label
            htmlFor="detectTrafficJams"
            className="form-check-label"
            data-toggle="tooltip"
            data-placement="top"
            data-html="true"
            title="You need to generate <b>cars</b> to detect traffic jams."
          >
            Detect traffic jams
          </label>
        </div>
        <div className="form-check user-select-none">
          <input
            type="checkbox"
            checked={generateBusFailures}
            disabled={!generatePedestrians || wasStarted}
            className="form-check-input"
            id="generateBusFailures"
            onChange={evSetGenerateBusFailures}
          />
          <label
            htmlFor="generateBusFailures"
            className="form-check-label"
            data-toggle="tooltip"
            data-placement="top"
            data-html="true"
            title="You need to generate <b>buses</b> to generate bus failures."
          >
            Generate bus failures
          </label>
        </div>
      </div>

      <div className="mb-4 form-border">
        <div className="custom-control custom-switch user-select-none">
          <input
            type="checkbox"
            className="custom-control-input"
            id="lightStrategyActive"
            checked={lightStrategyActive}
            disabled={!(generateCars || generatePedestrians || generateBikes) || wasStarted}
            onChange={evSetLightStrategyActive}
          />
          <label
            htmlFor="lightStrategyActive"
            className="custom-control-label"
            data-toggle="tooltip"
            data-placement="top"
            data-html="true"
            title="You need to generate <b>cars</b> or <b>bikes</b> or <b>pedestrians</b> to use this strategy."
          >
            Light strategy
          </label>
        </div>
        {lightStrategyActive && (
          <div className="form-group mt-2" key={`ls-${configState}`}>
            <label htmlFor="extendLightTime">Extension time</label>
            <input
              type="number"
              className="form-control"
              id="extendLightTime"
              disabled={wasStarted}
              defaultValue={extendLightTime}
              min={LIGHT_EXTEND_MIN}
              max={LIGHT_EXTEND_MAX}
              placeholder="Enter green lights extension time"
              onChange={evSetLightExtend}
            />
          </div>
        )}

        <div className="custom-control custom-switch user-select-none">
          <input
            type="checkbox"
            className="custom-control-input"
            id="stationStrategyActive"
            checked={stationStrategyActive}
            disabled={!generatePedestrians || wasStarted}
            onChange={evSetStationStrategyActive}
          />
          <label
            htmlFor="stationStrategyActive"
            className="custom-control-label"
            data-toggle="tooltip"
            data-placement="top"
            data-html="true"
            title="You need to generate <b>buses</b> to use this strategy."
          >
            Bus station strategy
          </label>
        </div>
        {stationStrategyActive && (
          <div className="form-group mt-2" key={`ss-${configState}`}>
            <label htmlFor="extendWaitTime">Extension time</label>
            <input
              type="number"
              className="form-control"
              id="extendWaitTime"
              defaultValue={extendWaitTime}
              disabled={wasStarted}
              placeholder="Enter bus wait extension time"
              onChange={evSetWaitExtend}
            />
          </div>
        )}

        <div className="custom-control custom-switch user-select-none">
          <input
            type="checkbox"
            className="custom-control-input"
            id="troublePointStrategyActive"
            checked={troublePointStrategyActive}
            disabled={!generateCars || !generateTroublePoints || wasStarted}
            onChange={evSetTroublePointStrategyActive}
          />
          <label
            htmlFor="troublePointStrategyActive"
            className="custom-control-label"
            data-toggle="tooltip"
            data-placement="top"
            data-html="true"
            title="You need to generate <b>cars</b> and <b>trouble points</b> to use this strategy."
          >
            Trouble point strategy
          </label>
        </div>

        <div className="custom-control custom-switch user-select-none">
          <input
            type="checkbox"
            className="custom-control-input"
            id="trafficJamStrategyActive"
            checked={trafficJamStrategyActive}
            disabled={!generateCars || !detectTrafficJams || wasStarted}
            onChange={evSetTrafficJamStrategyActive}
          />
          <label
            htmlFor="trafficJamStrategyActive"
            className="custom-control-label"
            data-toggle="tooltip"
            data-placement="top"
            data-html="true"
            title="You need to generate <b>cars</b> and <b>detect traffic jams</b> to use this strategy."
          >
            Traffic jam strategy
          </label>
        </div>

        <div className="custom-control custom-switch user-select-none">
          <input
            type="checkbox"
            className="custom-control-input"
            id="transportChangeStrategyActive"
            checked={transportChangeStrategyActive}
            disabled={!generatePedestrians || !generateBusFailures || wasStarted}
            onChange={evSetTransportChangeStrategyActive}
          />
          <label
            htmlFor="transportChangeStrategyActive"
            className="custom-control-label"
            data-toggle="tooltip"
            data-placement="top"
            data-html="true"
            title="You need to generate <b>buses</b> and <b>bus failures</b> to use this strategy."
          >
            Transport change strategy
          </label>
        </div>
      </div>
    </>
  );
};

const mapStateToProps = (state /* , ownProps */) => {
  const { wasStarted } = state.message;
  const {
    configState,
    prepareSimulationData: { generatePedestrians },
    startSimulationData,
  } = state.interaction;
  return {
    configState,
    wasStarted,
    startSimulationData,
    generatePedestrians,
  };
};

export default connect(mapStateToProps)(React.memo(StrategyMenu));
