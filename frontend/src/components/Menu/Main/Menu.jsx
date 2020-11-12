/* eslint-disable no-restricted-globals */
/* eslint-disable indent */
import { connect } from "react-redux";
import React from "react";

import Flatpickr from "react-flatpickr";

import { dispatch } from "../../../redux/store";
import { startSimulationDataUpdated } from "../../../redux/core/actions";
import PrepareMenu from "./PrepareMenu";
import GenerationSubMenu from "./GenerationSubMenu";
import SimulationStarter from "./SimulationStarter";

import CustomClock from "./CustomClock";
import { TIME_SCALE_MIN, TIME_SCALE_MAX } from "../../../constants/minMax";
import { setIfValidInt } from "../../../utils/helpers";

import "flatpickr/dist/themes/material_blue.css";
import "../../../styles/Menu.css";

const Menu = props => {
  const { configState, wasStarted, timeScaleConfig, timeStartConfig } = props;

  function evSetTime(newTime) {
    dispatch(startSimulationDataUpdated({ startTime: newTime[0] }));
  }

  function evSetTimeScale(e) {
    setIfValidInt(e, TIME_SCALE_MIN, TIME_SCALE_MAX, val => dispatch(startSimulationDataUpdated({ timeScale: val })));
  }

  return (
    <div>
      {!wasStarted && <PrepareMenu />}

      <form className="form-border">
        <GenerationSubMenu />

        <div className="mt-3" key={`sT-${configState}`}>
          <label htmlFor="simulationTime">Simulation start time</label>
          <Flatpickr
            key="simulationTime"
            options={{
              enableTime: true,
              dateFormat: "M d H:i",
              time_24hr: true,
              allowInput: true,
              wrap: true,
              defaultDate: timeStartConfig,
            }}
            onChange={evSetTime}
          >
            <input type="text" className="form-control" disabled={wasStarted} placeholder="Select Date.." data-input />
          </Flatpickr>
        </div>
        <div className="form-group mt-2" key={`tS-${configState}`}>
          <label htmlFor="timeScale">Time scale</label>
          <input
            type="number"
            defaultValue={timeScaleConfig}
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
  const {
    configState,
    startSimulationData: { timeScale, startTime },
  } = state.interaction;
  return {
    wasStarted,
    configState,
    timeScaleConfig: timeScale,
    timeStartConfig: startTime,
  };
};

export default connect(mapStateToProps)(React.memo(Menu));
