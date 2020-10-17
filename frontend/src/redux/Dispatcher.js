import { dispatch } from "./store";
import {
  carUpdated,
  carCreated,
  simulationPrepared,
  carKilled,
  lightsSwitched,
  troublePointCreated,
  simulationStarted,
  carRouteChanged,
} from "./actions";
import { batch } from "react-redux";

const fps = 15;
let timeScale = 1;
let timer = null;
let carUpdateQueue = new Map();
let switchLightsQueue = new Map();

function update() {
  batch(() => {
    carUpdateQueue.forEach(action => dispatch(action));
    switchLightsQueue.forEach(action => dispatch(action));
  });
  switchLightsQueue.clear();
}

export default {
  prepareSimulation(lights, stations) {
    dispatch(simulationPrepared({ lights, stations }));
  },

  startSimulation(newTimeScale) {
    timeScale = newTimeScale;
    timer = setInterval(update, 1000 / fps);
    dispatch(simulationStarted());
  },

  createCar(car) {
    dispatch(carCreated(car));
    if (timer == null) {
      batch(() => {
        this.prepareSimulation([], []);
        this.startSimulation(10);
      });
    }
  },

  updateCar(car) {
    carUpdateQueue.set(car.id, carUpdated(car));
  },

  killCar(id) {
    dispatch(carKilled(id));
  },

  updateCarRoute(routeInfo) {
    dispatch(carRouteChanged(routeInfo));
  },

  switchLights(id) {
    // WARN: May cause unwanted behaviour
    if (switchLightsQueue.has(id)) {
      switchLightsQueue.delete(id);
    } else {
      switchLightsQueue.set(id, lightsSwitched(id));
    }
  },

  createTroublePoint(troublePoint) {
    dispatch(troublePointCreated(troublePoint));
  },
};
