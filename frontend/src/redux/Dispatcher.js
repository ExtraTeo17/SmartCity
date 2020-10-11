import { dispatch } from "./store";
import { carUpdated, carCreated, lightLocationsUpdated } from "./actions";
import { batch } from "react-redux";

const fps = 15;
let timeScale = 1;
let timer;
let carUpdateQueue = [];

export default {
  prepareSimulation(lightLocations) {
    dispatch(lightLocationsUpdated(lightLocations));
  },

  startSimulation(newTimeScale) {
    timeScale = newTimeScale;
    timer = setInterval(() => {
      batch(() => {
        carUpdateQueue.forEach(action => dispatch(action));
      });
      carUpdateQueue = [];
    }, 1000 / fps);
  },

  createCar(car) {
    dispatch(carCreated(car));
  },

  updateCar(car) {
    carUpdateQueue.push(carUpdated(car));
  },
};
