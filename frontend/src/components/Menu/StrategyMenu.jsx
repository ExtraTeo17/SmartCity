/* eslint-disable no-restricted-globals */
import { connect } from "react-redux";
import React from "react";
import { dispatch } from "../../redux/store";
import { startSimulationDataUpdated } from "../../redux/core/actions";
import "../../styles/Menu.css";
import { setIfValidInt } from "../../utils/helpers";
import { LIGHT_EXTEND_MIN, LIGHT_EXTEND_MAX, STATION_EXTEND_MAX, STATION_EXTEND_MIN } from "../../constants/minMax";

const StrategyMenu = props => {
  const {
    configState,
    wasStarted,
    startSimulationData: {
      useFixedRoutes,
      useFixedTroublePoints,

      lightStrategyActive,
      extendLightTime,

      stationStrategyActive,
      extendWaitTime,

      changeRouteOnTroublePoint,
      changeRouteOnTrafficJam,
    },
    generatePedestrians,
    generateTroublePoints,
  } = props;

  function evSetLightStrategyActive(e) {
    dispatch(startSimulationDataUpdated({ lightStrategyActive: e.target.checked }));
  }

  function evSetLightExtend(e) {
    setIfValidInt(e, LIGHT_EXTEND_MIN, LIGHT_EXTEND_MAX, val => dispatch(startSimulationDataUpdated({ extendLightTime: val })));
  }

  function evSetStationStrategyActive(e) {
    dispatch(startSimulationDataUpdated({ stationStrategyActive: e.target.checked }));
  }

  function evSetWaitExtend(e) {
    setIfValidInt(e, STATION_EXTEND_MIN, STATION_EXTEND_MAX, val =>
      dispatch(startSimulationDataUpdated({ extendWaitTime: val }))
    );
  }

  function evSetChangeRouteOnTroublePoint(e) {
    dispatch(startSimulationDataUpdated({ changeRouteOnTroublePoint: e.target.checked }));
  }

  function evSetChangeRouteOnTrafficJam(e) {
    dispatch(startSimulationDataUpdated({ changeRouteOnTrafficJam: e.target.checked }));
  }

  function evSetUseFixedRoutes(e) {
    dispatch(startSimulationDataUpdated({ useFixedRoutes: e.target.checked }));
  }

  function evSetUseFixedTroublePoints(e) {
    dispatch(startSimulationDataUpdated({ useFixedTroublePoints: e.target.checked }));
  }

  return (
    <>
      <div className="mb-4 form-border">
        <div className="form-check user-select-none">
          <input
            type="checkbox"
            className="form-check-input"
            id="useFixedRoutes"
            checked={useFixedRoutes}
            disabled={wasStarted}
            onChange={evSetUseFixedRoutes}
          />
          <label htmlFor="useFixedRoutes" className="form-check-label">
            Use fixed routes
          </label>
        </div>
        <div className="form-check user-select-none">
          <input
            type="checkbox"
            className="form-check-input"
            id="useFixedTroublePoints"
            checked={useFixedTroublePoints}
            disabled={wasStarted || !useFixedRoutes}
            onChange={evSetUseFixedTroublePoints}
          />
          <label htmlFor="useFixedTroublePoints" className="form-check-label">
            Use fixed trouble points
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
      </div>
      {generatePedestrians && (
        <div className="mb-4 form-border">
          <div className="form-check user-select-none">
            <input
              type="checkbox"
              className="form-check-input"
              id="stationStrategyActive"
              checked={stationStrategyActive}
              disabled={wasStarted}
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
      )}

      {generateTroublePoints && (
        <div className="mb-4 form-border">
          <div className="form-check user-select-none">
            <input
              type="checkbox"
              className="form-check-input"
              id="changeRouteOnTroublePoint"
              checked={changeRouteOnTroublePoint}
              disabled={wasStarted}
              onChange={evSetChangeRouteOnTroublePoint}
            />
            <label htmlFor="changeRouteOnTroublePoint" className="form-check-label">
              Change route on trouble point
            </label>
          </div>
        </div>
      )}

      <div className="mb-4 form-border">
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
    generateTroublePoints: startSimulationData.generateTroublePoints,
  };
};

export default connect(mapStateToProps)(React.memo(StrategyMenu));
