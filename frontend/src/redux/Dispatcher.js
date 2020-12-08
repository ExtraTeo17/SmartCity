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
  trafficJamStarted,
  trafficJamEnded,
  bikeUpdated,
  bikeCreated,
  bikeKilled,
  batchedUpdate,
  busCrashed,
} from "./core/actions";

const fps = 20;
const fpsInterval = 1000 / fps;
let timer = null;

const carUpdateQueue = new Map();
const switchLightsQueue = new Map();
const busUpdateQueue = new Map();
const pedestrianUpdateQueue = new Map();
const bikeUpdateQueue = new Map();

const pushPullCount = 10;
const pullKillPedQueue = [];
const pushPedQueue = [];

let start = window.performance.now();

/**
 * @ignore
 * @param {any} action
 * @param {any} key
 * @param {Map} map
 */
function dispatchFromMap(action, key, map) {
  map.delete(key);
  dispatch(action);
}

/**
 * @ignore
 * @param {number} now
 */
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
      bikeUpdateQueue.forEach(dispatchFromMap);
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

let localWasPrepared = false;

function onDetectStartedSimulation() {
  if (localWasPrepared === true) {
    return;
  }

  batch(() => {
    // eslint-disable-next-line no-use-before-define
    Dispatcher.prepareSimulation([], [], []);
    // eslint-disable-next-line no-use-before-define
    Dispatcher.startSimulation();
  });
}

/**
 * @category Redux
 * @namespace Dispatcher
 */
const Dispatcher = {
  /**
   * @param {any[]} lights
   * @param {any[]} stations
   * @param {any[]} buses
   */
  prepareSimulation(lights, stations, buses) {
    localWasPrepared = true;
    dispatch(simulationPrepared({ lights, stations, buses }));
  },

  startSimulation() {
    localWasPrepared = false;
    timer = update(window.performance.now());
    dispatch(simulationStarted());
  },

  /**
   * @param {any} car
   */
  createCar(car) {
    dispatch(carCreated(car));
    if (timer === null) {
      onDetectStartedSimulation();
    }
  },

  /**
   * @param {{ id: any }} car
   */
  updateCar(car) {
    carUpdateQueue.set(car.id, carUpdated(car));
  },

  /**
   * @param {any} data
   */
  killCar(data) {
    dispatch(carKilled(data));
  },

  /**
   * @param {any} routeInfo
   */
  updateCarRoute(routeInfo) {
    dispatch(carRouteChanged(routeInfo));
  },

  /**
   * @param {any} id
   */
  switchLights(id) {
    // WARN: May cause unwanted behaviour
    if (switchLightsQueue.has(id)) {
      switchLightsQueue.delete(id);
    } else {
      switchLightsQueue.set(id, lightsSwitched(id));
    }
  },

  /**
   * @param {any} troublePoint
   */
  createTroublePoint(troublePoint) {
    dispatch(troublePointCreated(troublePoint));
  },

  /**
   * @param {any} id
   */
  hideTroublePoint(id) {
    dispatch(troublePointVanished(id));
  },

  /**
   * @param {any} id
   */
  startTrafficJam(id) {
    dispatch(trafficJamStarted(id));
  },

  /**
   * @param {any} id
   */
  endTrafficJam(id) {
    dispatch(trafficJamEnded(id));
  },

  /**
   * @param {{ id: any }} bus
   */
  updateBus(bus) {
    busUpdateQueue.set(bus.id, busUpdated(bus));
  },

  /**
   * @param {any} busData
   */
  updateBusFillState(busData) {
    dispatch(busFillStateUpdated(busData));
    if (timer === null) {
      onDetectStartedSimulation();
    }
  },

  /**
   * @param {any} id
   */
  killBus(id) {
    dispatch(busKilled(id));
    busUpdateQueue.delete(id);
  },

  /**
   * @param {any} id
   */
  crashBus(id) {
    dispatch(busCrashed(id));
    busUpdateQueue.delete(id);
  },

  /**
   * @param {any} pedestrian
   */
  createPedestrian(pedestrian) {
    dispatch(pedestrianCreated(pedestrian));
    if (timer === null) {
      onDetectStartedSimulation();
    }
  },

  /**
   * @param {{ id: any }} pedData
   */
  updatePedestrian(pedData) {
    pedestrianUpdateQueue.set(pedData.id, pedestrianUpdated(pedData));
  },

  /**
   * @param {any} id
   */
  pushPedestrianIntoBus(id) {
    pushPedQueue.push(pedestrianPushedIntoBus(id));
  },

  /**
   * @param {any} pedData
   */
  pullPedestrianFromBus(pedData) {
    pullKillPedQueue.push(pedestrianPulledFromBus(pedData));
  },

  /**
   * @param {any} pedData
   */
  killPedestrian(pedData) {
    pullKillPedQueue.push(pedestrianKilled(pedData));
  },

  /**
   * @param {any} bike
   */
  createBike(bike) {
    dispatch(bikeCreated(bike));
    if (timer === null) {
      onDetectStartedSimulation();
    }
  },

  /**
   * @param {{ id: any }} bike
   */
  updateBike(bike) {
    bikeUpdateQueue.set(bike.id, bikeUpdated(bike));
  },

  /**
   * @param {any} data
   */
  killBike(data) {
    dispatch(bikeKilled(data));
  },

  /**
   * @param {Object[]} carUpdates
   * @param {Object[]} bikeUpdates
   * @param {Object[]} busUpdates
   * @param {Object[]} pedUpdates
   */
  updateBatched(carUpdates, bikeUpdates, busUpdates, pedUpdates) {
    dispatch(batchedUpdate({ carUpdates, bikeUpdates, busUpdates, pedUpdates }));
  },
};

export default Dispatcher;
