import { connect } from "react-redux";
import React, { useEffect } from "react";

import "flatpickr/dist/themes/material_blue.css";

import ApiManager from "../../../web/ApiManager";
import "../../../styles/Menu.css";
import { dispatch } from "../../../redux/store";
import { shouldStartSimulation } from "../../../redux/core/actions";
import { StartState } from "../../../redux/models/states";

export const SimulationStarterObj = props => {
  const { wasPrepared, startState, wasStarted, startSimulationData } = props;

  const startSimulationProceed = () => {
    if (startState === StartState.Invoke) {
      dispatch(shouldStartSimulation());
    } else if (startState === StartState.Proceed) {
      ApiManager.startSimulation(startSimulationData);
    }
  };
  useEffect(startSimulationProceed, [startState]);

  const startSimulationInvoke = () => {
    dispatch(shouldStartSimulation());
  };

  return (
    <div className="center-wrapper">
      <button
        className="btn btn-success mt-3"
        disabled={wasStarted || !wasPrepared}
        title={
          wasStarted ? "Simulation already started" : wasPrepared ? "Simulation is ready to run" : "You must prepare simulation!"
        }
        type="button"
        onClick={startSimulationInvoke}
      >
        Start simulation
      </button>
    </div>
  );
};

const mapStateToProps = (state /* ownProps */) => {
  const { wasPrepared, wasStarted } = state.message;
  const { startSimulationData, startState } = state.interaction;
  return {
    wasPrepared,
    wasStarted,
    startState,
    startSimulationData,
  };
};

export default connect(mapStateToProps)(
  React.memo(SimulationStarterObj, (prevprops, newProps) => {
    return (
      prevprops.wasPrepared === newProps.wasPrepared &&
      prevprops.wasStarted === newProps.wasStarted &&
      prevprops.startState === newProps.startState
    );
  })
);
