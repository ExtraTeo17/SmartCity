import { batch } from "react-redux";
import { dispatch } from "./store";
import {
  carUpdated,
  carCreated,
  simulationPrepared,
  carKilled,
  lightsSwitched,
  troublePointCreated,
  troublePointVanished,
  simulationStarted,
  carRouteChanged,
  busUpdated,
  busFillStateUpdated,
  busKilled,
  pedestrianCreated,
  pedestrianUpdated,
  pedestrianPushedIntoBus,
  pedestrianPulledFromBus,
  pedestrianKilled,
} from "./actions";

const fps = 15;
const fpsInterval = 1000 / fps;
let timeScale = 1;
let timer = null;

const carUpdateQueue = new Map();
const switchLightsQueue = new Map();
const busUpdateQueue = new Map();
const pedestrianUpdateQueue = new Map();

const pushPullCount = 10;
const pullKillPedQueue = [];
const pushPedQueue = [];

let start = window.performance.now();

function dispatchFromMap(action, key, map) {
  map.delete(key);
  dispatch(action);
}

function update(now) {
  const elapsed = now - start;

  if (elapsed > fpsInterval) {
    // https://stackoverflow.com/a/19772220/6841224
    start = now - (elapsed % fpsInterval);
    batch(() => {
      carUpdateQueue.forEach(dispatchFromMap);
      switchLightsQueue.forEach(dispatchFromMap);
      busUpdateQueue.forEach(dispatchFromMap);
      pedestrianUpdateQueue.forEach(dispatchFromMap);
      pushPedQueue.forEach(a => dispatch(a));
      if (pullKillPedQueue.length > 0) {
        const end = Math.min(pullKillPedQueue.length, pushPullCount);
        for (let i = 0; i < end; ++i) {
          const a = pullKillPedQueue.pop();
          dispatch(a);
        }
      }
    });
    pushPedQueue.length = 0;
  }

  window.requestAnimationFrame(update);
}

function onDetectStartedSimulation() {
  batch(() => {
    // eslint-disable-next-line no-use-before-define
    Dispatcher.prepareSimulation([], [], []);
    // eslint-disable-next-line no-use-before-define
    Dispatcher.startSimulation(10);
  });
}

const Dispatcher = {
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

  hideTroublePoint(id) {
    dispatch(troublePointVanished(id));
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
    busUpdateQueue.delete(id);
  },

  createPedestrian(pedestrian) {
    dispatch(pedestrianCreated(pedestrian));
    if (timer === null) {
      onDetectStartedSimulation();
    }
  },

  updatePedestrian(pedData) {
    pedestrianUpdateQueue.set(pedData.id, pedestrianUpdated(pedData));
  },

  pushPedestrianIntoBus(id) {
    pushPedQueue.push(pedestrianPushedIntoBus(id));
  },

  pullPedestrianFromBus(pedData) {
    pullKillPedQueue.push(pedestrianPulledFromBus(pedData));
  },

  killPedestrian(id) {
    pullKillPedQueue.push(pedestrianKilled(id));
  },
};

export default Dispatcher;
