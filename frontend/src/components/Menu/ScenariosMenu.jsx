import React from "react";
import { connect } from "react-redux";
import { configReplaced, simulationPrepareStarted } from "../../redux/core/actions";
import { dispatch } from "../../redux/store";
import ApiManager from "../../web/ApiManager";
import { IS_DEBUG } from "../../constants/global";
import { initialInteractionState } from "../../redux/reducers/interaction";

import "../../styles/Menu.css";

export const ScenariosMenuObj = props => {
  const { inPreparation, wasStarted, config = initialInteractionState } = props;

  function prepareSimulation(data) {
    if (ApiManager.isConnected()) {
      dispatch(simulationPrepareStarted());
      ApiManager.prepareSimulation(data);
    }
  }

  function prepareLightScenario() {
    const state = config;
    state.prepareSimulationData.center = { lat: 52.23682, lng: 21.01681, rad: 600 };
    state.prepareSimulationData.generatePedestrians = false;
    state.startSimulationData.generateCars = true;
    state.startSimulationData.carsLimit = 10;
    state.startSimulationData.testCarId = 5;
    dispatch(configReplaced(state));
    prepareSimulation(state.prepareSimulationData);
  }

  function prepareBusStationsScenario() {
    const state = config;
    state.prepareSimulationData.center = { lat: 52.203342, lng: 20.861213, rad: 300 };
    state.prepareSimulationData.generatePedestrians = true;
    state.startSimulationData.generateCars = false;
    dispatch(configReplaced(state));
    prepareSimulation(state.prepareSimulationData);
  }

  function prepareTroublePointsScenario() {
    // TODO: fill data
    const state = config;
    dispatch(configReplaced(state));
    prepareSimulation(state.prepareSimulationData);
  }

  function prepareTrafficJamsScenario() {
    // TODO: fill data
    const state = config;
    dispatch(configReplaced(state));
    prepareSimulation(state.prepareSimulationData);
  }

  function prepareTransportChangeScenario() {
    // TODO: fill data
    const state = config;
    dispatch(configReplaced(state));
    prepareSimulation(state.prepareSimulationData);
  }

  return (
    <div className="form-border">
      <button
        id="lightScenarioBtn"
        className="btn btn-primary btn-block"
        type="button"
        disabled={inPreparation || wasStarted}
        onClick={prepareLightScenario}
      >
        Light scenario
      </button>

      <button
        id="prepareBusStationsScenarioBtn"
        className="btn btn-light btn-block mt-5"
        type="button"
        disabled={inPreparation || wasStarted}
        onClick={prepareBusStationsScenario}
      >
        Bus stations scenario
      </button>

      <button
        id="prepareTroublePointsScenario"
        className="btn btn-primary btn-block mt-5"
        type="button"
        disabled={inPreparation || wasStarted}
        onClick={prepareTroublePointsScenario}
      >
        Trouble points scenario
      </button>

      <button
        id="prepareTrafficJamsScenarioBtn"
        className="btn btn-light btn-block mt-5"
        type="button"
        disabled={inPreparation || wasStarted}
        onClick={prepareTrafficJamsScenario}
      >
        Traffic jams scenario
      </button>

      <button
        id="prepareTransportChangeScenarioBtn"
        className="btn btn-primary btn-block mt-5"
        type="button"
        disabled={inPreparation || wasStarted}
        onClick={prepareTransportChangeScenario}
      >
        Transport change scenario
      </button>

      {IS_DEBUG && (
        <button className="btn btn-primary btn-block mt-5" type="button" onClick={ApiManager.debug}>
          Debug
        </button>
      )}
    </div>
  );
};

const mapStateToProps = (state /* , ownProps */) => {
  const { inPreparation, wasStarted } = state.message;
  return {
    inPreparation,
    wasStarted,
    config: state.interaction,
  };
};

export default connect(mapStateToProps)(React.memo(ScenariosMenuObj));
