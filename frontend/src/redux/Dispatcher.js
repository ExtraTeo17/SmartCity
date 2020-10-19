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
} from "./actions";
import { batch } from "react-redux";

const fps = 15;
let timeScale = 1;
let timer = null;
const carUpdateQueue = new Map();
const switchLightsQueue = new Map();
const busUpdateQueue = new Map();

function update() {
  batch(() => {
    carUpdateQueue.forEach(action => dispatch(action));
    switchLightsQueue.forEach(action => dispatch(action));
    busUpdateQueue.forEach(action => dispatch(action));
  });
  switchLightsQueue.clear();
}

export default {
  prepareSimulation(lights, stations, buses) {
    dispatch(simulationPrepared({ lights, stations, buses }));
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

  updateBus(bus) {
    busUpdateQueue.set(bus.id, busUpdated(bus));
  },

  updateBusFillState(busData) {
    dispatch(busFillStateUpdated(busData));
    console.group("Bus-fill-" + busData.id);
    console.info(busData);
    console.groupEnd();

    if (timer === null) {
      batch(() => {
        this.prepareSimulation([], []);
        this.startSimulation(10);
      });
    }
  },
};
