import React from "react";
import { connect } from "react-redux";
import { configReplaced, simulationPrepareStarted } from "../../redux/core/actions";
import { dispatch } from "../../redux/store";
import ApiManager from "../../web/ApiManager";
import { IS_DEBUG } from "../../constants/global";
import { initialInteractionState } from "../../redux/reducers/interaction";
import { notifyWaitForConnection } from "../../utils/helpers";

import "../../styles/Menu.css";

/**
 * Menu tab, which holds all scenarios, i.e. predefined settings for all configurations. <br/>
 * Each scenario has coresponding strategy in StrategyMenu.
 * Each scenario button prepares the simulation automatically.
 * @category Menu
 * @module ScenariosMenu
 */
export const ScenariosMenuObj = props => {
  const { inPreparation, wasStarted, config = initialInteractionState } = props;

  function prepareSimulation(data) {
    if (ApiManager.isConnected()) {
      dispatch(simulationPrepareStarted());
      ApiManager.prepareSimulation(data);
    } else {
      notifyWaitForConnection();
    }
  }

  function prepareLightScenario() {
    const state = config;
    const prepareData = state.prepareSimulationData;
    const startData = state.startSimulationData;

    prepareData.center = { lat: 52.23643, lng: 21.01448, rad: 370 };

    startData.generateCars = true;
    startData.carsLimit = 10;
    startData.testCarId = 5;
    startData.generateBatchesForCars = false;
    startData.generateBikes = true;
    startData.bikesLimit = 8;
    startData.testBikeId = 4;

    startData.useFixedRoutes = true;
    startData.startTime = new Date();
    startData.timeScale = 10;
    startData.lightStrategyActive = true;
    startData.extendLightTime = 20;

    prepareData.generatePedestrians = false;
    startData.generateTroublePoints = false;
    startData.generateBusFailures = false;
    startData.detectTrafficJams = false;

    startData.stationStrategyActive = false;
    startData.troublePointStrategyActive = false;
    startData.trafficJamStrategyActive = false;
    startData.transportChangeStrategyActive = false;

    dispatch(configReplaced(state));
    prepareSimulation(state.prepareSimulationData);
  }

  function prepareBusStationsScenario() {
    const state = config;
    const prepareData = state.prepareSimulationData;
    const startData = state.startSimulationData;

    prepareData.center = { lat: 52.20334, lng: 20.86121, rad: 300 };

    prepareData.generatePedestrians = true;
    startData.pedLimit = 20;
    startData.testPedId = 5;

    startData.useFixedRoutes = true;
    const date = new Date();
    date.setHours(8);
    date.setMinutes(42);
    date.setSeconds(0);
    startData.startTime = date;
    startData.timeScale = 10;
    startData.stationStrategyActive = true;
    startData.extendWaitTime = 60;

    startData.generateCars = false;
    startData.generateBatchesForCars = false;
    startData.generateBikes = false;
    startData.generateTroublePoints = false;
    startData.generateBusFailures = false;
    startData.detectTrafficJams = false;

    startData.lightStrategyActive = false;
    startData.troublePointStrategyActive = false;
    startData.trafficJamStrategyActive = false;
    startData.transportChangeStrategyActive = false;

    dispatch(configReplaced(state));
    prepareSimulation(state.prepareSimulationData);
  }

  function prepareTroublePointsScenario() {
    const state = config;
    const prepareData = state.prepareSimulationData;
    const startData = state.startSimulationData;

    prepareData.center = { lat: 52.25808, lng: 21.16241, rad: 610 };

    startData.generateCars = true;
    startData.carsLimit = 10;
    startData.testCarId = 5;
    startData.generateBatchesForCars = false;

    startData.useFixedRoutes = true;
    startData.startTime = new Date();
    startData.timeScale = 7;
    startData.generateTroublePoints = true;
    startData.timeBeforeTrouble = 5;
    startData.troublePointStrategyActive = true;
    startData.troublePointThresholdUntilIndexChange = 50;
    startData.noTroublePointStrategyIndexFactor = 30;

    prepareData.generatePedestrians = false;
    startData.generateBikes = false;
    startData.generateBusFailures = false;
    startData.detectTrafficJams = false;

    startData.lightStrategyActive = false;
    startData.stationStrategyActive = false;
    startData.trafficJamStrategyActive = false;
    startData.transportChangeStrategyActive = false;

    dispatch(configReplaced(state));
    prepareSimulation(state.prepareSimulationData);
  }

  function prepareTrafficJamsScenario() {
    const state = config;
    const prepareData = state.prepareSimulationData;
    const startData = state.startSimulationData;

    prepareData.center = { lat: 52.27633, lng: 20.95363, rad: 430 };

    startData.generateCars = true;
    startData.carsLimit = 15;
    startData.testCarId = 11;
    startData.generateBatchesForCars = true;

    startData.useFixedRoutes = true;
    startData.startTime = new Date();
    startData.timeScale = 3;
    startData.detectTrafficJams = true;
    startData.trafficJamStrategyActive = true;

    prepareData.generatePedestrians = false;
    startData.generateBikes = false;
    startData.generateTroublePoints = false;
    startData.generateBusFailures = false;

    startData.lightStrategyActive = false;
    startData.stationStrategyActive = false;
    startData.troublePointStrategyActive = false;
    startData.transportChangeStrategyActive = false;

    dispatch(configReplaced(state));
    prepareSimulation(state.prepareSimulationData);
  }

  function prepareTransportChangeScenario() {
    const state = config;
    const prepareData = state.prepareSimulationData;
    const startData = state.startSimulationData;

    prepareData.center = { lat: 52.20199, lng: 20.97899, rad: 341 };

    prepareData.generatePedestrians = true;
    startData.pedLimit = 20;
    startData.testPedId = 5;

    startData.useFixedRoutes = true;
    const date = getTransportChangeDate();
    startData.startTime = date;
    startData.timeScale = 10;
    startData.generateBusFailures = true;
    startData.transportChangeStrategyActive = true;

    startData.generateCars = false;
    startData.generateBatchesForCars = false;
    startData.generateBikes = false;
    startData.generateTroublePoints = false;
    startData.detectTrafficJams = false;

    startData.lightStrategyActive = false;
    startData.stationStrategyActive = false;
    startData.troublePointStrategyActive = false;
    startData.trafficJamStrategyActive = false;

    dispatch(configReplaced(state));
    prepareSimulation(state.prepareSimulationData);
  }

  function getTransportChangeDate() {
    const date = new Date();

    const day = date.getDay();
    const isWeekend = day === 6 || day === 0;
    if (isWeekend) {
      date.setHours(9);
      date.setMinutes(5);
    } else {
      date.setHours(8);
      date.setMinutes(40);
    }
    date.setSeconds(0);

    return date;
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
