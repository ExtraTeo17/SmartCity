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
  busUpdated,
  busFillStateUpdated,
  busKilled,
} from "./actions";
import { batch } from "react-redux";

const fps = 15;
const fpsInterval = 1000 / fps;
let timeScale = 1;
let timer = null;
const carUpdateQueue = new Map();
const switchLightsQueue = new Map();
const busUpdateQueue = new Map();
let start = window.performance.now();

function update(now) {
  const elapsed = now - start;

  if (elapsed > fpsInterval) {
    // https://stackoverflow.com/a/19772220/6841224
    start = now - (elapsed % fpsInterval);
    batch(() => {
      carUpdateQueue.forEach(action => dispatch(action));
      switchLightsQueue.forEach(action => dispatch(action));
      busUpdateQueue.forEach(action => dispatch(action));
    });
    switchLightsQueue.clear();
  }

  window.requestAnimationFrame(update);
}

function onDetectStartedSimulation() {
  batch(() => {
    this.prepareSimulation([], [], []);
    this.startSimulation(10);
  });
}

export default {
  prepareSimulation(lights, stations, buses) {
    dispatch(simulationPrepared({ lights, stations, buses }));
  },

  startSimulation(newTimeScale) {
    timeScale = newTimeScale;
    timer = update(window.performance.now());
    dispatch(simulationStarted());
  },

  createCar(car) {
    dispatch(carCreated(car));
    if (timer === null) {
      onDetectStartedSimulation();
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

  updateBus(bus) {
    busUpdateQueue.set(bus.id, busUpdated(bus));
  },

  updateBusFillState(busData) {
    dispatch(busFillStateUpdated(busData));
    if (timer === null) {
      onDetectStartedSimulation();
    }
  },

  killBus(id) {
    dispatch(busKilled(id));
  },
};
