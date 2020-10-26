/* eslint-disable no-restricted-globals */
/* eslint-disable indent */
import { connect } from "react-redux";
import React from "react";
import Flatpickr from "react-flatpickr";
import "flatpickr/dist/themes/material_blue.css";

import { dispatch } from "../../../redux/store";
import { startSimulationDataUpdated } from "../../../redux/actions";
import ApiManager from "../../../web/ApiManager";
import "../../../styles/Menu.css";
import PrepareMenu from "./PrepareMenu";
import CarsMenu from "./CarsMenu";

const Menu = ({ wasPrepared, wasStarted, startSimulationData }) => {
  function setTime(newTime) {
    dispatch(startSimulationDataUpdated({ time: newTime }));
  }

  const startSimulation = () => {
    ApiManager.startSimulation(startSimulationData);
  };

  return (
    <div>
      <PrepareMenu />

      <form className="form-border">
        <CarsMenu />

        <div className="mt-3">
          <label htmlFor="simulationTime">Simulation time</label>
          <Flatpickr
            key="simulationTime"
            options={{
              enableTime: true,
              dateFormat: "M d H:i",
              time_24hr: true,
              allowInput: true,
              wrap: true,
              defaultDate: startSimulationData.time,
            }}
            onChange={setTime}
          >
            <input type="text" className="form-control" disabled={wasStarted} placeholder="Select Date.." data-input />
          </Flatpickr>
        </div>
        <div className="center-wrapper">
          <button
            className="btn btn-success mt-3"
            disabled={wasStarted || !wasPrepared}
            title={
              wasStarted
                ? "Simulation already started"
                : wasPrepared
                ? "Simulation is ready to run"
                : "You must prepare simulation!"
            }
            type="button"
            onClick={startSimulation}
          >
            Start simulation
          </button>
        </div>
      </form>
    </div>
  );
};

const mapStateToProps = (state /* , ownProps */) => {
  const { wasPrepared, wasStarted } = state.message;
  return {
    wasPrepared,
    wasStarted,
    startSimulationData: state.interaction.startSimulationData,
  };
};

export default connect(mapStateToProps)(React.memo(Menu));
