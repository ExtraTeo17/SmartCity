import React from "react";
import { render, unmountComponentAtNode } from "react-dom";
import { act } from "react-dom/test-utils";
import { ScenariosMenuObj } from "../components/Menu/ScenariosMenu";
import { ConfigState } from "../redux/models/states";
import { PREPARE_SIMULATION_REQUEST } from "../web/MessageType";
// eslint-disable-next-line no-unused-vars
import WebServer from "../web/WebServer";
import * as actions from "../redux/core/actions";
import * as store from "../redux/store";

// mocks
let message;
WebServer.send = msg => {
  message = msg;
};
WebServer.isConnected = () => true;

let configPayload;
actions.configReplaced = payload => {
  configPayload = payload;
};
store.dispatch = () => {};

// store
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

  troublePointStrategyActive: true,
  trafficJamStrategyActive: false,
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

const configCopy = JSON.parse(JSON.stringify(config));
configCopy.startSimulationData.startTime = startSimulationData.startTime;

let container = null;
let buttons = null;
beforeAll(() => {
  container = document.createElement("div");
  document.body.appendChild(container);
  act(() => {
    render(<ScenariosMenuObj config={configCopy} wasStarted={false} />, container);
    buttons = document.getElementsByTagName("button");
  });
});

afterAll(() => {
  unmountComponentAtNode(container);
  container.remove();
  container = null;
  jest.clearAllMocks();
});

describe("Menu", () => {
  it("renders buttons correctly", () => {
    expect(buttons.length > 0).toBeTruthy();
  });

  it("passes correct data to ApiManager ", () => {
    for (let i = 0; i < buttons.length; ++i) {
      const button = buttons[i];
      console.info(button.id);

      message = null;
      act(() => {
        button.dispatchEvent(new MouseEvent("click", { bubbles: true }));
      });

      expect(message).toBeTruthy();
      expect(message.type).toBe(PREPARE_SIMULATION_REQUEST);
      expect(message.payload).toBeTruthy();
      expect(message.payload).toEqual(
        expect.objectContaining({
          latitude: expect.any(Number),
          longitude: expect.any(Number),
          radius: expect.any(Number),
          generatePedestrians: expect.any(Boolean),
        })
      );
    }
  });

  it("passes correct config to store", () => {
    for (let i = 0; i < buttons.length; ++i) {
      const button = buttons[i];
      console.info(button.id);

      configPayload = null;
      act(() => {
        button.dispatchEvent(new MouseEvent("click", { bubbles: true }));
      });

      expect(configPayload).toBeTruthy();

      expect(configPayload.prepareSimulationData).toEqual(
        expect.objectContaining({
          center: {
            lat: expect.any(Number),
            lng: expect.any(Number),
            rad: expect.any(Number),
          },
          generatePedestrians: expect.any(Boolean),
        })
      );

      // Divided into 3 for easier bug search
      expect(configPayload.startSimulationData).toEqual(
        expect.objectContaining({
          pedLimit: expect.any(Number),
          testPedId: expect.any(Number),

          generateCars: expect.any(Boolean),
          carsLimit: expect.any(Number),
          testCarId: expect.any(Number),

          generateBikes: expect.any(Boolean),
          bikesLimit: expect.any(Number),
          testBikeId: expect.any(Number),
        })
      );

      expect(configPayload.startSimulationData).toEqual(
        expect.objectContaining({
          generateTroublePoints: expect.any(Boolean),
          timeBeforeTrouble: expect.any(Number),

          useFixedRoutes: expect.any(Boolean),
          useFixedTroublePoints: expect.any(Boolean),
          startTime: expect.any(Date),
          timeScale: expect.any(Number),
        })
      );

      expect(configPayload.startSimulationData).toEqual(
        expect.objectContaining({
          lightStrategyActive: expect.any(Boolean),
          extendLightTime: expect.any(Number),

          stationStrategyActive: expect.any(Boolean),
          extendWaitTime: expect.any(Number),

          troublePointStrategyActive: expect.any(Boolean),
          trafficJamStrategyActive: expect.any(Boolean),
        })
      );
    }
  });
});
