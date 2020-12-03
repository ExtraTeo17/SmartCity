/* eslint-disable no-restricted-globals */
import { connect } from "react-redux";
import React from "react";
import { dispatch } from "../../redux/store";
import { startSimulationDataUpdated, generatePedestriansUpdated } from "../../redux/core/actions";
import "../../styles/Menu.css";
import { setIfValidInt } from "../../utils/helpers";
import { LIGHT_EXTEND_MIN, LIGHT_EXTEND_MAX, STATION_EXTEND_MAX, STATION_EXTEND_MIN } from "../../constants/minMax";

const StrategyMenu = props => {
  const {
    configState,
    wasStarted,
    startSimulationData: {
      generateCars,
      generateBikes,
      generateTroublePoints,

      lightStrategyActive,
      extendLightTime,

      stationStrategyActive,
      extendWaitTime,

      changeRouteOnTroublePoint,
      changeRouteOnTrafficJam,
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

  function evSetChangeRouteOnTroublePoint(e) {
    dispatchUpdate({ changeRouteOnTroublePoint: e.target.checked });
  }

  function evSetChangeRouteOnTrafficJam(e) {
    dispatchUpdate({ changeRouteOnTrafficJam: e.target.checked });
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

        <div className="form-check user-select-none">
          <input
            type="checkbox"
            checked={generatePedestrians}
            className="form-check-input"
            id="generatePedestrians"
            onChange={evSetGeneratePedestrians}
          />
          <label htmlFor="generatePedestrians" className="form-check-label">
            Generate buses & pedestrians & stations
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
          <label htmlFor="generateTroublePoints" className="form-check-label">
            Generate trouble points
          </label>
        </div>
      </div>

      <div className="mb-4 form-border">
        <div className="form-check user-select-none">
          <input
            type="checkbox"
            className="form-check-input"
            id="lightStrategyActive"
            checked={lightStrategyActive}
            disabled={wasStarted}
            onChange={evSetLightStrategyActive}
          />
          <label htmlFor="lightStrategyActive" className="form-check-label">
            Light strategy
          </label>
        </div>
        {lightStrategyActive && (
          <div className="form-group mt-2" key={`ls-${configState}`}>
            <label htmlFor="extendLightTime">Green lights extension time</label>
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
        <div className="form-check user-select-none">
          <input
            type="checkbox"
            className="form-check-input"
            id="changeRouteOnTrafficJam"
            checked={changeRouteOnTrafficJam}
            disabled={wasStarted}
            onChange={evSetChangeRouteOnTrafficJam}
          />
          <label htmlFor="changeRouteOnTrafficJam" className="form-check-label">
            Change route on traffic jam
          </label>
        </div>
      </div>
      <div className="mb-4 form-border">
        <div className="form-check user-select-none">
          <input
            type="checkbox"
            className="form-check-input"
            id="stationStrategyActive"
            checked={stationStrategyActive}
            disabled={!generatePedestrians || wasStarted}
            onChange={evSetStationStrategyActive}
          />
          <label htmlFor="stationStrategyActive" className="form-check-label">
            Station strategy
          </label>
        </div>
        {stationStrategyActive && (
          <div className="form-group mt-2" key={`ss-${configState}`}>
            <label htmlFor="extendWaitTime">Bus extension wait time</label>
            <input
              type="number"
              className="form-control"
              id="extendWaitTime"
              defaultValue={extendWaitTime}
              disabled={wasStarted}
              placeholder="Enter bus extension wait time"
              onChange={evSetWaitExtend}
            />
          </div>
        )}
      </div>

      <div className="mb-4 form-border">
        <div className="form-check user-select-none">
          <input
            type="checkbox"
            className="form-check-input"
            id="changeRouteOnTroublePoint"
            checked={changeRouteOnTroublePoint}
            disabled={!generateTroublePoints || wasStarted}
            onChange={evSetChangeRouteOnTroublePoint}
          />
          <label htmlFor="changeRouteOnTroublePoint" className="form-check-label">
            Change route on trouble point
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
