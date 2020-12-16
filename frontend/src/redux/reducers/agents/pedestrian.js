import {
  PEDESTRIAN_CREATED,
  PEDESTRIAN_UPDATED,
  PEDESTRIAN_KILLED,
  PEDESTRIAN_PUSHED,
  PEDESTRIAN_PULLED,
  BATCHED_UPDATE,
} from "../../core/constants";

/**
 * Handles pedestrian-agent-related interaction
 * @category Redux
 * @subcategory Reducers
 * @module pedestrian
 */

// Just for reference - defined in store.js
const initialState = {
  pedestrians: [],
};

const deletedPedestrianIds = [];

const pedestrian = (state = initialState, action) => {
  const { payload } = action;
  switch (action.type) {
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

    case BATCHED_UPDATE: {
      const { pedUpdates } = payload;

      let any = false;
      const newPedestrians = state.pedestrians.map(p => {
        if (!p.hidden) {
          const update = pedUpdates.find(ped => ped.id === p.id);
          if (update && update.location !== p.location) {
            any = true;
            return { ...p, location: update.location };
          }
        }
        return p;
      });

      if (!any) {
        return state;
      }

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
          return { ...p, location: pedData.location, hidden: false, route: pedData.showRoute ? p.routeFromStation : [] };
        }
        return p;
      });

      return { ...state, pedestrians: newPedestrians };
    }

    case PEDESTRIAN_KILLED: {
      const { id } = payload;

      const newPedestrians = state.pedestrians.filter(p => p.id !== id);
      deletedPedestrianIds.push(id);

      return { ...state, pedestrians: newPedestrians };
    }

    default:
      return state;
  }
};

export default pedestrian;
