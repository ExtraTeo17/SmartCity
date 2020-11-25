import React from "react";
import { connect } from "react-redux";
import { configReplaced } from "../../redux/core/actions";
import { dispatch } from "../../redux/store";
import ApiManager from "../../web/ApiManager";
import "../../styles/Menu.css";
import { IS_DEBUG } from "../../constants/global";
import { initialInteractionState } from "../../redux/reducers/interaction";

export const ScenariosMenuObj = props => {
  const { wasStarted, config = initialInteractionState } = props;

  const prepareSimulation = data => {
    ApiManager.prepareSimulation(data);
  };

  const prepareCarZone = () => {
    const state = config;
    state.prepareSimulationData.center = { lat: 52.23682, lng: 21.01681, rad: 600 };
    state.prepareSimulationData.generatePedestrians = false;
    state.startSimulationData.generateCars = true;
    state.startSimulationData.carsLimit = 10;
    state.startSimulationData.testCarId = 5;
    dispatch(configReplaced(state));
    prepareSimulation(state.prepareSimulationData);
  };

  const prepareBusZone = () => {
    const state = config;
    state.prepareSimulationData.center = { lat: 52.203342, lng: 20.861213, rad: 300 };
    state.prepareSimulationData.generatePedestrians = true;
    state.startSimulationData.generateCars = false;
    dispatch(configReplaced(state));
    prepareSimulation(state.prepareSimulationData);
  };

  const prepareRandomZoneForCars = () => {
    const state = config;
    state.prepareSimulationData.center = { lat: 52.24492, lng: 21.08439, rad: 300 };
    state.prepareSimulationData.generatePedestrians = false;
    state.startSimulationData.generateCars = true;
    state.startSimulationData.carsLimit = 10;
    state.startSimulationData.timeScale = 9;
    dispatch(configReplaced(state));
    prepareSimulation(state.prepareSimulationData);
  };

  const prepareAllInOneZone = () => {
    const state = config;
    state.prepareSimulationData.center = { lat: 52.19221, lng: 20.89436, rad: 300 };
    state.prepareSimulationData.generatePedestrians = true;
    state.startSimulationData = {
      pedLimit: 12,
      testPedId: 2,

      generateCars: true,
      carsLimit: 5,
      testCarId: 3,

      generateBikes: true,
      bikesLimit: 6,
      testBikeId: 4,

      generateTroublePoints: true,
      timeBeforeTrouble: 6,

      useFixedRoutes: true,
      useFixedTroublePoints: true,
      startTime: new Date(),
      timeScale: 7,

      lightStrategyActive: true,
      extendLightTime: 33,

      stationStrategyActive: true,
      extendWaitTime: 61,

      changeRouteOnTroublePoint: true,
      changeRouteOnTrafficJam: true,
    };
    dispatch(configReplaced(state));
    prepareSimulation(state.prepareSimulationData);
  };

  return (
    <div className="form-border">
      <button
        id="prepareCarZoneBtn"
        className="btn btn-primary btn-block"
        type="button"
        disabled={wasStarted}
        onClick={prepareCarZone}
      >
        Prepare car zone
      </button>

      <button
        id="prepareBusZoneBtn"
        className="btn btn-primary btn-block mt-5"
        type="button"
        disabled={wasStarted}
        onClick={prepareBusZone}
      >
        Prepare bus zone
      </button>

      <button
        id="prepareRandomZoneBtn"
        className="btn btn-light btn-block mt-5"
        type="button"
        disabled={wasStarted}
        onClick={prepareRandomZoneForCars}
      >
        Prepare random zone for cars
      </button>

      <button
        id="prepareAllZoneBtn"
        className="btn btn-light btn-block mt-5"
        type="button"
        disabled={wasStarted}
        onClick={prepareAllInOneZone}
      >
        Prepare zone for all
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
  const { wasStarted } = state.message;
  return {
    wasStarted,
    config: state.interaction,
  };
};

export default connect(mapStateToProps)(React.memo(ScenariosMenuObj));
