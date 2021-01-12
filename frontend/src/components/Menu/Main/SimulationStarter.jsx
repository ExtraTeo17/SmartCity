import { connect } from "react-redux";
import React from "react";

import ApiManager from "../../../web/ApiManager";

import { dispatch } from "../../../redux/store";
import { shouldStartSimulation } from "../../../redux/core/actions";
import { notifyWaitForConnection } from "../../../utils/helpers";

import "flatpickr/dist/themes/material_blue.css";
import "../../../styles/Menu.css";

export const SimulationStarterObj = props => {
  const { wasPrepared, wasStarted, startSimulationData } = props;

  const startSimulationInvoke = () => {
    if (ApiManager.isConnected()) {
      dispatch(shouldStartSimulation());
      ApiManager.startSimulation(startSimulationData);
    } else {
      notifyWaitForConnection();
    }
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
  const { startSimulationData } = state.interaction;
  return {
    wasPrepared,
    wasStarted,
    startSimulationData,
  };
};

export default connect(mapStateToProps)(React.memo(SimulationStarterObj));
