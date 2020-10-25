import { BusFillState } from "../../components/Models/BusFillState";
import { LightColor } from "../../components/Models/LightColor";
import {
  CAR_KILLED,
  CAR_CREATED,
  CAR_UPDATED,
  SIMULATION_PREPARED,
  LIGHTS_SWITCHED,
  TROUBLE_POINT_CREATED,
  SIMULATION_STARTED,
  CAR_ROUTE_CHANGED,
  BUS_UPDATED,
  BUS_FILL_STATE_UPDATED,
  BUS_KILLED,
  PEDESTRIAN_CREATED,
  PEDESTRIAN_UPDATED,
  PEDESTRIAN_KILLED,
  PEDESTRIAN_PUSHED,
  PEDESTRIAN_PULLED,
} from "../constants";

// Just for reference - defined in store.js
const initialState = {
  lights: [],
  cars: [],
  stations: [],
  buses: [],
  pedestrians: [],
  troublePoints: [],
  wasPrepared: false,
  wasStarted: false,
};

const deletedCarIds = [];
const deletedBusIds = [];
const deletedPedestrianIds = [];

const message = (state = initialState, action) => {
  const { payload } = action;
  switch (action.type) {
    case SIMULATION_PREPARED: {
      const { lights, stations, buses } = action.payload;
      return {
        ...state,
        lights,
        stations,
        buses,
        wasPrepared: true,
      };
    }

    case SIMULATION_STARTED: {
      return { ...state, wasStarted: true };
    }

    case CAR_CREATED: {
      const { car } = action.payload;
      return { ...state, cars: [...state.cars, car] };
    }

    case CAR_UPDATED: {
      const car = action.payload;

      let unrecognized = true;
      const newCars = state.cars.map(c => {
        if (c.id === car.id) {
          unrecognized = false;
          return { ...c, location: car.location };
        }
        return c;
      });

      if (unrecognized === true && !deletedCarIds.includes(car.id)) {
        newCars.push(car);
      }

      return { ...state, cars: newCars };
    }

    case CAR_KILLED: {
      const id = action.payload;
      const newCars = state.cars.filter(c => c.id !== id);
      deletedCarIds.push(id);

      return { ...state, cars: newCars };
    }

    case CAR_ROUTE_CHANGED: {
      const { id, routeStart, routeEnd, location } = payload;
      const newCars = state.cars.map(c => {
        if (c.id === id) {
          return { ...c, route: [...routeStart, ...routeEnd], routeChangePoint: location };
        }
        return c;
      });

      return { ...state, cars: newCars };
    }

    case LIGHTS_SWITCHED: {
      const id = action.payload;

      const newLights = state.lights.map(oldLight => {
        if (oldLight.groupId === id) {
          return { ...oldLight, color: oldLight.color === LightColor.GREEN ? LightColor.RED : LightColor.GREEN };
        }

        return oldLight;
      });

      return {
        ...state,
        lights: newLights,
      };
    }

    case TROUBLE_POINT_CREATED: {
      const troublePoint = action.payload;

      return { ...state, troublePoints: [...state.troublePoints, troublePoint] };
    }

    case BUS_UPDATED: {
      const bus = action.payload;

      let unrecognized = true;
      const newBuses = state.buses.map(b => {
        if (b.id === bus.id) {
          unrecognized = false;
          return { ...b, location: bus.location };
        }
        return b;
      });

      if (unrecognized === true && !deletedBusIds.includes(bus.id)) {
        newBuses.push({ ...bus, fillState: BusFillState.LOW });
      }

      return { ...state, buses: newBuses };
    }

    case BUS_FILL_STATE_UPDATED: {
      const busData = payload;
      console.groupCollapsed(`Update bus fill-${busData.id}`);
      console.info(busData);
      console.groupEnd();
      const newBuses = state.buses.map(b => {
        if (b.id === busData.id) {
          return { ...b, fillState: busData.fillState };
        }
        return b;
      });

      return { ...state, buses: newBuses };
    }

    case BUS_KILLED: {
      console.info(`Killed bus: ${payload}`);
      const id = payload;
      const newBuses = state.buses.filter(b => b.id !== id);
      deletedBusIds.push(id);

      return { ...state, buses: newBuses };
    }

    case PEDESTRIAN_CREATED: {
      const pedestrian = { ...payload, route: payload.routeToStation };
      return { ...state, pedestrians: [...state.pedestrians, pedestrian] };
    }

    case PEDESTRIAN_UPDATED: {
      const ped = payload;

      const newPedestrians = state.pedestrians.map(p => {
        if (p.id === ped.id) {
          return { ...p, location: ped.location };
        }
        return p;
      });

      return { ...state, pedestrians: newPedestrians };
    }

    case PEDESTRIAN_PUSHED: {
      const id = payload;
      const newPedestrians = state.pedestrians.map(p => {
        if (p.id === id) {
          return { ...p, hidden: true };
        }
        return p;
      });

      return { ...state, pedestrians: newPedestrians };
    }

    case PEDESTRIAN_PULLED: {
      const pedData = payload;
      if (deletedPedestrianIds.includes(pedData.id)) {
        return state;
      }

      const newPedestrians = state.pedestrians.map(p => {
        if (p.id === pedData.id) {
          return { ...p, location: pedData.location, hidden: false, route: p.routeFromStation };
        }
        return p;
      });

      return { ...state, pedestrians: newPedestrians };
    }

    case PEDESTRIAN_KILLED: {
      const id = payload;

      const newPedestrians = state.pedestrians.filter(p => p.id !== id);
      deletedPedestrianIds.push(id);

      return { ...state, pedestrians: newPedestrians };
    }

    default:
      return state;
  }
};

export default message;
