/* eslint-disable no-restricted-globals */
/* eslint-disable indent */
import { connect } from "react-redux";
import React, { useEffect, useState } from "react";

import Flatpickr from "react-flatpickr";
import "flatpickr/dist/themes/material_blue.css";

import { dispatch } from "../../../redux/store";
import { startSimulationDataUpdated } from "../../../redux/core/actions";
import PrepareMenu from "./PrepareMenu";
import GenerationSubMenu from "./GenerationSubMenu";
import SimulationStarter from "./SimulationStarter";
import "../../../styles/Menu.css";
import { StartState } from "../../../redux/models/startState";
import CustomClock from "./CustomClock";
import { D_START_TIME, D_TIME_SCALE } from "../../../constants/defaults";
import { TIME_SCALE_MIN, TIME_SCALE_MAX } from "../../../constants/minMax";
import { setIfValidInt } from "../../../utils/helpers";

const Menu = props => {
  const { wasStarted, shouldStart } = props;
  const [startTime, setTime] = useState(D_START_TIME);
  const [timeScale, setTimeScale] = useState(D_TIME_SCALE);
  const onStart = () => {
    if (shouldStart === StartState.Invoke) {
      dispatch(startSimulationDataUpdated({ startTime, timeScale }));
    }
  };
  useEffect(onStart, [shouldStart]);

  function evSetTime(newTime) {
    setTime(newTime[0]);
  }

  function evSetTimeScale(e) {
    setIfValidInt(e, TIME_SCALE_MIN, TIME_SCALE_MAX, setTimeScale);
  }

  return (
    <div>
      {!wasStarted && <PrepareMenu />}

      <form className="form-border">
        <GenerationSubMenu />

        <div className="mt-3">
          <label htmlFor="simulationTime">Simulation start time</label>
          <Flatpickr
            key="simulationTime"
            options={{
              enableTime: true,
              dateFormat: "M d H:i",
              time_24hr: true,
              allowInput: true,
              wrap: true,
              defaultDate: startTime,
            }}
            onChange={evSetTime}
          >
            <input type="text" className="form-control" disabled={wasStarted} placeholder="Select Date.." data-input />
          </Flatpickr>
        </div>
        <div className="form-group mt-2">
          <label htmlFor="timeScale">Time scale</label>
          <input
            type="number"
            defaultValue={timeScale}
            disabled={wasStarted}
            min={TIME_SCALE_MIN}
            max={TIME_SCALE_MAX}
            className="form-control"
            id="timeScale"
            title="Increase scale to speed up time"
            onChange={evSetTimeScale}
          />
        </div>
        <SimulationStarter />
      </form>

      {wasStarted && <CustomClock />}
    </div>
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

export default connect(mapStateToProps)(React.memo(Menu));
