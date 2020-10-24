/* eslint-disable no-restricted-globals */
/* eslint-disable indent */
import { connect } from "react-redux";
import React, { useState } from "react";
import Flatpickr from "react-flatpickr";
import "flatpickr/dist/themes/material_blue.css";

import ApiManager from "../../web/ApiManager";
import "../../styles/Menu.css";
import PrepareMenu from "./PrepareMenu";

const DEFAULT_CARS_NUM = 4;
const DEFAULT_TEST_CAR = 2;

const Menu = props => {
  const { wasPrepared, wasStarted } = props;

  const [carsNum, setCarsNum] = useState(DEFAULT_CARS_NUM);
  const [testCarNum, setTestCarNum] = useState(DEFAULT_TEST_CAR);
  const [generateCars, setGenerateCars] = useState(true);
  const [generatePedestrians, setGeneratePedestrians] = useState(true);
  const [generateTroublePoints, setGenerateTroublePoints] = useState(true);
  const [time, setTime] = useState(new Date());

  const carMin = 1;
  const carMax = 50;

  const startSimulation = () => {
    ApiManager.startSimulation({
      carsNum,
      testCarNum,
      generateCars,
      generatePedestrians,
      generateTroublePoints: generateCars && generateTroublePoints,
    });
  };

  return (
    <div>
      <PrepareMenu />
      <form className="form-border">
        <div className="form-group">
          <label htmlFor="carsNum">Cars limit</label>
          <input
            type="number"
            defaultValue={carsNum}
            className="form-control"
            id="carsNum"
            min={carMin}
            max={carMax}
            placeholder="Enter limit for cars"
            onChange={e => setCarsNum(parseInt(e.target.value, 10))}
          />
        </div>
        <div className="form-group">
          <label htmlFor="testCarNum">Test car number</label>
          <input
            type="number"
            defaultValue={testCarNum}
            className="form-control"
            id="testCarNum"
            min={1}
            max={1000}
            placeholder="Enter test car number"
            onChange={e => setTestCarNum(parseInt(e.target.value, 10))}
          />
        </div>
        <div className="form-check user-select-none">
          <input
            type="checkbox"
            checked={generateCars}
            className="form-check-input"
            id="generateCars"
            onChange={e => setGenerateCars(e.target.checked)}
          />
          <label htmlFor="generateCars" className="form-check-label">
            Generate cars
          </label>
        </div>
        <div className="form-check user-select-none">
          <input
            type="checkbox"
            checked={generatePedestrians}
            className="form-check-input"
            id="generatePedestrians"
            onChange={e => setGeneratePedestrians(e.target.checked)}
          />
          <label htmlFor="generatePedestrians" className="form-check-label">
            Generate pedestrians
          </label>
        </div>
        <div className="form-check user-select-none">
          <input
            type="checkbox"
            checked={generateTroublePoints}
            disabled={!generateCars}
            className="form-check-input"
            id="generateTroublePoints"
            onChange={e => setGenerateTroublePoints(e.target.checked)}
          />
          <label htmlFor="generateTroublePoints" className="form-check-label">
            Generate trouble points
          </label>
        </div>
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
            }}
            value={time}
            onChange={setTime}
          >
            <input type="text" className="form-control" placeholder="Select Date.." data-input />
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
    center: state.interaction.center,
    wasPrepared,
    wasStarted,
  };
};

export default connect(mapStateToProps)(Menu);
