import { BusFillState } from "../../../components/Models/BusFillState";
import { SIMULATION_PREPARED, BUS_UPDATED, BUS_FILL_STATE_UPDATED, BUS_KILLED, BATCHED_UPDATE } from "../../core/constants";

// Just for reference - defined in store.js
const initialState = {
  buses: [],
};

const deletedBusIds = [];

const bus = (state = initialState, action) => {
  const { payload } = action;
  switch (action.type) {
    case SIMULATION_PREPARED: {
      return { ...state, buses: action.payload.buses };
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
        if (update) {
          return { ...b, location: update.location };
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
      console.info(`Killed bus: ${payload}`);
      const id = payload;
      const newBuses = state.buses.filter(b => b.id !== id);
      deletedBusIds.push(id);

      return { ...state, buses: newBuses };
    }

    default:
      return state;
  }
};

export default bus;
