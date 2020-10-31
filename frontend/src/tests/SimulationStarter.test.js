// user.test.js

import React from "react";
import { render, unmountComponentAtNode } from "react-dom";
import { act } from "react-dom/test-utils";
import { SimulationStarter } from "../components/Menu/Main/SimulationStarter";
import { StartState } from "../redux/models/startState";
import { START_SIMULATION_REQUEST } from "../web/MessageType";
import WebServer from "../web/WebServer";

let message;
jest.mock("../web/WebServer", () => {
  return {
    send(msg) {
      message = msg;
    },
  };
});

let container = null;
beforeEach(() => {
  // setup a DOM element as a render target
  container = document.createElement("div");
  document.body.appendChild(container);
});

afterEach(() => {
  // cleanup on exiting
  unmountComponentAtNode(container);
  container.remove();
  container = null;
});

const startSimulationData = {
  pedLimit: 1,
  testPedId: 2,

  generateCars: true,
  carsLimit: 22,
  testCarId: 10,

  generateBikes: true,
  bikesLimit: 55,
  testBikeId: 4,

  generateTroublePoints: false,
  timeBeforeTrouble: 5,

  startTime: new Date("2017-02-05T12:00:00Z"),

  lightStrategyActive: true,
  extendLightTime: 30,

  stationStrategyActive: false,
  extendWaitTime: 60,

  changeRouteOnTroublePoint: true,
  changeRouteOnTrafficJam: false,
};

it("Passes correct data to ApiManager", async () => {
  // Use the asynchronous version of act to apply resolved promises
  act(() => {
    render(
      <SimulationStarter
        shouldStart={StartState.Proceed}
        startSimulationData={startSimulationData}
        wasPrepared
        wasStarted={false}
      />,
      container
    );
  });

  expect(message).toBeTruthy();
  expect(message.type).toBe(START_SIMULATION_REQUEST);
  expect(message.payload).toBeTruthy();
  expect(message.payload).toMatchObject({
    pedLimit: startSimulationData.pedLimit,
    testPedId: startSimulationData.testPedId,

    generateCars: startSimulationData.generateCars,
    carsLimit: startSimulationData.carsLimit,
    testCarId: startSimulationData.testCarId,

    generateBikes: startSimulationData.generateBikes,
    bikesLimit: startSimulationData.bikesLimit,
    testBikeId: startSimulationData.testBikeId,

    generateTroublePoints: startSimulationData.generateTroublePoints,
    timeBeforeTrouble: startSimulationData.timeBeforeTrouble,

    startTime: startSimulationData.startTime,

    lightStrategyActive: startSimulationData.lightStrategyActive,
    extendLightTime: startSimulationData.extendLightTime,

    stationStrategyActive: startSimulationData.stationStrategyActive,
    extendWaitTime: startSimulationData.extendWaitTime,

    changeRouteOnTroublePoint: startSimulationData.changeRouteOnTroublePoint,
    changeRouteOnTrafficJam: startSimulationData.changeRouteOnTrafficJam,
  });
});
