import React from "react";
import { render, unmountComponentAtNode } from "react-dom";
import { act } from "react-dom/test-utils";
import { SimulationStarterObj } from "../components/Menu/Main/SimulationStarter";
import { START_SIMULATION_REQUEST } from "../web/MessageType";
import WebServer from "../web/WebServer";
import * as store from "../redux/store";

let message;
WebServer.send = msg => {
  message = msg;
};
store.dispatch = () => {};

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

  useFixedRoutes: true,
  useFixedTroublePoints: false,
  startTime: new Date("2017-02-05T12:00:00Z"),
  timeScale: 12,

  lightStrategyActive: true,
  extendLightTime: 30,

  stationStrategyActive: false,
  extendWaitTime: 60,

  changeRouteOnTroublePoint: true,
  changeRouteOnTrafficJam: false,
};

it("Passes correct data to ApiManager", () => {
  act(() => {
    render(<SimulationStarterObj startSimulationData={startSimulationData} wasPrepared wasStarted={false} />, container);
  });

  const button = container.querySelector("button");
  act(() => {
    button.dispatchEvent(new MouseEvent("click", { bubbles: true }));
  });

  expect(message).toBeTruthy();
  expect(message.type).toBe(START_SIMULATION_REQUEST);
  expect(message.payload).toBeTruthy();
  expect(message.payload).toStrictEqual({
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

    useFixedRoutes: startSimulationData.useFixedRoutes,
    useFixedTroublePoints: startSimulationData.useFixedTroublePoints,
    startTime: startSimulationData.startTime,
    timeScale: startSimulationData.timeScale,

    lightStrategyActive: startSimulationData.lightStrategyActive,
    extendLightTime: startSimulationData.extendLightTime,

    stationStrategyActive: startSimulationData.stationStrategyActive,
    extendWaitTime: startSimulationData.extendWaitTime,

    changeRouteOnTroublePoint: startSimulationData.changeRouteOnTroublePoint,
    changeRouteOnTrafficJam: startSimulationData.changeRouteOnTrafficJam,
  });
});
