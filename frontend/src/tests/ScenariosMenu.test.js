// user.test.js

import React from "react";
import { render, unmountComponentAtNode } from "react-dom";
import { act } from "react-dom/test-utils";
import { ScenariosMenuObj } from "../components/Menu/ScenariosMenu";
import { ConfigState } from "../redux/models/states";
import { PREPARE_SIMULATION_REQUEST } from "../web/MessageType";
// eslint-disable-next-line no-unused-vars
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

const prepareSimulationData = {
  center: { lat: 52.2, lng: 20.1, rad: 300 },
  generatePedestrians: false,
};

const config = {
  configState: ConfigState.Initial,
  prepareSimulationData,
  startSimulationData,
};

it("Passes correct data to ApiManager", async () => {
  // Use the asynchronous version of act to apply resolved promises
  act(() => {
    render(<ScenariosMenuObj config={config} wasStarted={false} />, container);
  });

  const button = document.getElementById("prepareCarZoneBtn");
  act(() => {
    button.dispatchEvent(new MouseEvent("click", { bubbles: true }));
  });

  expect(message).toBeTruthy();
  expect(message.type).toBe(PREPARE_SIMULATION_REQUEST);
  expect(message.payload).toBeTruthy();
  expect({
    latitude: prepareSimulationData.center.lat,
    longitude: prepareSimulationData.center.lng,
    radius: prepareSimulationData.center.rad,
    generatePedestrians: prepareSimulationData.generatePedestrians,
  }).toMatchObject(message.payload);
});
