/* eslint-disable no-restricted-globals */
import { connect } from "react-redux";
import React, { useState, useEffect } from "react";
import { StartState } from "../../redux/models/startState";
import { dispatch } from "../../redux/store";
import { startSimulationDataUpdated } from "../../redux/actions";
import "../../styles/Menu.css";

const StrategyMenu = props => {
  const { wasStarted, shouldStart } = props;
  const [lightStrategyActive, setLightStrategyActive] = useState(true);
  const [extendLightTime, setExtendLightTime] = useState(30);

  const [stationStrategyActive, setStationStrategyActive] = useState(true);
  const [extendWaitTime, setExtendWaitTime] = useState(60);

  const [changeRouteStrategyActive, setChangeRouteStrategyActive] = useState(true);

  const onStart = () => {
    if (shouldStart === StartState.Invoke) {
      dispatch(
        startSimulationDataUpdated({
          lightStrategyActive,
          extendLightTime,
          stationStrategyActive,
          extendWaitTime,
          changeRouteStrategyActive,
        })
      );
    }
  };
  useEffect(onStart, [shouldStart]);

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
            Light strategy active
          </label>
        </div>
        {lightStrategyActive && (
          <div className="form-group mt-2">
            <label htmlFor="extendLightTime">Green lights extension time</label>
            <input
              type="number"
              defaultValue={extendLightTime}
              className="form-control"
              id="extendLightTime"
              placeholder="Enter green lights extension time"
              onChange={e => setExtendLightTime(e.target.value)}
            />
          </div>
        )}
      </div>
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
            Station strategy active
          </label>
        </div>
        {stationStrategyActive && (
          <div className="form-group mt-2">
            <label htmlFor="extendWaitTime">Enter bus extension wait time</label>
            <input
              type="number"
              defaultValue={extendWaitTime}
              disabled={wasStarted}
              className="form-control"
              id="extendWaitTime"
              placeholder="Enter bus extension wait time"
              onChange={e => setExtendWaitTime(e.target.value)}
            />
          </div>
        )}
      </div>

      <div className="mb-4 form-border">
        <div className="form-check user-select-none">
          <input
            type="checkbox"
            checked={changeRouteStrategyActive}
            disabled={wasStarted}
            className="form-check-input"
            id="changeRouteStrategyActive"
            onChange={e => setChangeRouteStrategyActive(e.target.checked)}
          />
          <label htmlFor="changeRouteStrategyActive" className="form-check-label">
            Change route strategy active
          </label>
        </div>
      </div>
    </>
  );
};

const mapStateToProps = (state /* , ownProps */) => {
  const { wasStarted } = state.message;
  const { shouldStart } = state.interaction;
  return {
    wasStarted,
    shouldStart,
  };
};

export default connect(mapStateToProps)(React.memo(StrategyMenu));
