import { BusFillState } from "../../../components/Models/BusFillState";
import {
  SIMULATION_PREPARED,
  BUS_UPDATED,
  BUS_FILL_STATE_UPDATED,
  BUS_KILLED,
  BATCHED_UPDATE,
  BUS_CRASHED,
} from "../../core/constants";

/**
 * Handles bus-agent-related interaction
 *  - SIMULATION_PREPARED
 *  - BUS_UPDATED
 *  - BATCHED_UPDATE
 *  - BUS_FILL_STATE_UPDATED
 *  - BUS_KILLED
 *  - BUS_CRASHED
 *
 * @category Redux
 * @subcategory Reducers
 * @module bus
 */

// Just for reference - defined in store.js
const initialState = {
  buses: [],
};

const deletedBusIds = [];

const bus = (state = initialState, action) => {
  const { payload } = action;
  switch (action.type) {
    case SIMULATION_PREPARED: {
      const buses = payload.buses.map(b => {
        return { ...b, startedMoving: false, crashed: false };
      });

      return { ...state, buses };
    }

    case BUS_UPDATED: {
      const bus = action.payload;

      let unrecognized = true;
      const newBuses = state.buses.map(b => {
        if (b.id === bus.id) {
          unrecognized = false;
          return { ...b, location: bus.location, startedMoving: true };
        }
        return b;
      });

      if (unrecognized === true && !deletedBusIds.includes(bus.id)) {
        newBuses.push({ ...bus, fillState: BusFillState.LOW });
      }

      return { ...state, buses: newBuses };
    }

    case BATCHED_UPDATE: {
      const { busUpdates } = payload;

      const newBuses = state.buses.map(b => {
        const update = busUpdates.find(bus => bus.id === b.id);
        if (update && b.location !== update.location) {
          return { ...b, location: update.location, startedMoving: true };
        }
        return b;
      });

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
      const id = payload;
      console.info(`Killed bus: ${id}`);

      const newBuses = state.buses.filter(b => b.id !== id);
      deletedBusIds.push(id);

      return { ...state, buses: newBuses };
    }

    case BUS_CRASHED: {
      const id = payload;
      console.info(`Crashed bus: ${payload}`);
      const newBuses = state.buses.map(b => {
        if (b.id === id) {
          return { ...b, crashed: true };
        }
        return b;
      });

      return { ...state, buses: newBuses };
    }

    default:
      return state;
  }
};

export default bus;
