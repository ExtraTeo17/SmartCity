/* eslint-disable no-restricted-globals */
import { connect } from "react-redux";
import React, { useState, useEffect } from "react";
import { ConfigState, StartState } from "../../redux/models/states";
import { dispatch } from "../../redux/store";
import { startSimulationDataUpdated } from "../../redux/core/actions";
import "../../styles/Menu.css";
import { setIfValidInt } from "../../utils/helpers";
import { LIGHT_EXTEND_MIN, LIGHT_EXTEND_MAX, STATION_EXTEND_MAX, STATION_EXTEND_MIN } from "../../constants/minMax";
import {
  D_EXTEND_WAIT_TIME,
  D_LIGHT_STRATEGY_ACTIVE,
  D_STATION_STRATEGY_ACTIVE,
  D_EXTEND_LIGHT_TIME,
  D_CHANGE_ROUTE_TP_ACTIVE,
  D_CHANGE_ROUTE_TJ_ACTIVE,
} from "../../constants/defaults";

const StrategyMenu = props => {
  const { configState, startState, wasStarted, startSimulationData, generatePedestrians, generateTroublePoints } = props;
  const [lightStrategyActive, setLightStrategyActive] = useState(D_LIGHT_STRATEGY_ACTIVE);
  const [extendLightTime, setExtendLightTime] = useState(D_EXTEND_LIGHT_TIME);

  const [stationStrategyActive, setStationStrategyActive] = useState(D_STATION_STRATEGY_ACTIVE);
  const [extendWaitTime, setExtendWaitTime] = useState(D_EXTEND_WAIT_TIME);

  const [changeRouteOnTroublePoint, setchangeRouteOnTroublePoint] = useState(D_CHANGE_ROUTE_TP_ACTIVE);
  const [changeRouteOnTrafficJam, setchangeRouteOnTrafficJam] = useState(D_CHANGE_ROUTE_TJ_ACTIVE);

  const onStart = () => {
    if (startState === StartState.Invoke) {
      dispatch(
        startSimulationDataUpdated({
          lightStrategyActive,
          extendLightTime,
          stationStrategyActive,
          extendWaitTime,
          changeRouteOnTroublePoint,
          changeRouteOnTrafficJam,
        })
      );
    }
  };
  useEffect(onStart, [startState]);

  function onReplace() {
    if (configState !== ConfigState.Initial) {
      setLightStrategyActive(startSimulationData.lightStrategyActive);
      setExtendLightTime(startSimulationData.extendLightTime);

      setStationStrategyActive(startSimulationData.stationStrategyActive);
      setExtendWaitTime(startSimulationData.extendWaitTime);

      setchangeRouteOnTroublePoint(startSimulationData.changeRouteOnTroublePoint);
      setchangeRouteOnTrafficJam(startSimulationData.changeRouteOnTrafficJam);
    }
  }
  useEffect(onReplace, [configState]);

  function evSetLightExtend(e) {
    setIfValidInt(e, LIGHT_EXTEND_MIN, LIGHT_EXTEND_MAX, setExtendLightTime);
  }

  function evSetWaitExtend(e) {
    setIfValidInt(e, STATION_EXTEND_MIN, STATION_EXTEND_MAX, setExtendWaitTime);
  }

  function evSetchangeRouteOnTrafficJam(e) {
    setchangeRouteOnTrafficJam(e.target.checked);
  }

  return (
    <>
      <div className="mb-4 form-border">
        <div className="form-check user-select-none">
          <input
            type="checkbox"
            checked={lightStrategyActive}
            disabled={wasStarted}
            className="form-check-input"
            id="lightStrategyActive"
            onChange={e => setLightStrategyActive(e.target.checked)}
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
              disabled={wasStarted}
              defaultValue={extendLightTime}
              min={LIGHT_EXTEND_MIN}
              max={LIGHT_EXTEND_MAX}
              className="form-control"
              id="extendLightTime"
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
              checked={stationStrategyActive}
              disabled={wasStarted}
              className="form-check-input"
              id="stationStrategyActive"
              onChange={e => setStationStrategyActive(e.target.checked)}
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
                defaultValue={extendWaitTime}
                disabled={wasStarted}
                className="form-control"
                id="extendWaitTime"
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
              checked={changeRouteOnTroublePoint}
              disabled={wasStarted}
              className="form-check-input"
              id="changeRouteOnTroublePoint"
              onChange={e => setchangeRouteOnTroublePoint(e.target.checked)}
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
            checked={changeRouteOnTrafficJam}
            disabled={wasStarted}
            className="form-check-input"
            id="changeRouteOnTrafficJam"
            onChange={evSetchangeRouteOnTrafficJam}
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
    startState,
    prepareSimulationData: { generatePedestrians },
    startSimulationData,
  } = state.interaction;
  return {
    configState,
    startState,
    wasStarted,
    startSimulationData,
    generatePedestrians,
    generateTroublePoints: startSimulationData.generateTroublePoints,
  };
};

export default connect(mapStateToProps)(React.memo(StrategyMenu));
