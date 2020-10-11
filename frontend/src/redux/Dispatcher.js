import { dispatch } from "./store";
import { carUpdated, carCreated, lightsCreated, carKilled, lightsSwitched } from "./actions";
import { batch } from "react-redux";

const fps = 15;
let timeScale = 1;
let timer;
let carUpdateQueue = [];
let switchLightsQueue = [];

export default {
  prepareSimulation(lights) {
    dispatch(lightsCreated(lights));
  },

  startSimulation(newTimeScale) {
    timeScale = newTimeScale;
    timer = setInterval(() => {
      batch(() => {
        carUpdateQueue.forEach(action => dispatch(action));
        switchLightsQueue.forEach(action => dispatch(action));
      });
      carUpdateQueue = [];
      switchLightsQueue = [];
    }, 1000 / fps);
  },

  createCar(car) {
    dispatch(carCreated(car));
  },

  updateCar(car) {
    carUpdateQueue.push(carUpdated(car));
  },

  killCar(id) {
    dispatch(carKilled(id));
  },

  switchLights(id) {
    switchLightsQueue.push(lightsSwitched(id));
  },
};
