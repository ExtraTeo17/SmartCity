import { dispatch } from "./store";
import { carUpdated, carCreated, simulationPrepared, carKilled, lightsSwitched } from "./actions";
import { batch } from "react-redux";

const fps = 15;
let timeScale = 1;
let timer;
let carUpdateQueue = new Map();
let switchLightsQueue = new Map();

export default {
  prepareSimulation(lights, stations) {
    dispatch(simulationPrepared({ lights, stations }));
  },

  startSimulation(newTimeScale) {
    timeScale = newTimeScale;
    timer = setInterval(() => {
      batch(() => {
        carUpdateQueue.forEach(action => dispatch(action));
        switchLightsQueue.forEach(action => dispatch(action));
      });
      switchLightsQueue.clear();
    }, 1000 / fps);
  },

  createCar(car) {
    dispatch(carCreated(car));
  },

  updateCar(car) {
    carUpdateQueue.set(car.id, carUpdated(car));
  },

  killCar(id) {
    dispatch(carKilled(id));
  },

  switchLights(id) {
    // WARN: May cause unwanted behaviour
    if (switchLightsQueue.has(id)) {
      switchLightsQueue.delete(id);
    } else {
      switchLightsQueue.set(id, lightsSwitched(id));
    }
  },
};
