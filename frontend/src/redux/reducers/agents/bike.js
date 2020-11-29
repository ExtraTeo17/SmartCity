import { BIKE_KILLED, BIKE_CREATED, BIKE_UPDATED, BATCHED_UPDATE } from "../../core/constants";

// Just for reference - defined in store.js
const initialState = {
  bikes: [],
};

const deletedBikeIds = [];

const bike = (state = initialState, action) => {
  const { payload } = action;
  switch (action.type) {
    case BIKE_CREATED: {
      const bike = payload;
      return { ...state, bikes: [...state.bikes, bike] };
    }

    case BIKE_UPDATED: {
      const bike = payload;

      let unrecognized = true;
      const newBikes = state.bikes.map(c => {
        if (c.id === bike.id) {
          unrecognized = false;
          return { ...c, location: bike.location };
        }
        return c;
      });

      if (unrecognized === true && !deletedBikeIds.includes(bike.id)) {
        newBikes.push(bike);
      }

      return { ...state, bikes: newBikes };
    }

    case BATCHED_UPDATE: {
      const { bikeUpdates } = payload;

      const newBikes = state.bikes.map(b => {
        const update = bikeUpdates.find(bike => bike.id === b.id);
        if (update) {
          return { ...b, location: update.location };
        }
        return b;
      });

      return { ...state, bikes: newBikes };
    }

    case BIKE_KILLED: {
      const { id } = payload;
      const newBikes = state.bikes.filter(c => c.id !== id);
      deletedBikeIds.push(id);

      return { ...state, bikes: newBikes };
    }

    default:
      return state;
  }
};

export default bike;
