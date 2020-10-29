import { BIKE_KILLED, BIKE_CREATED, BIKE_UPDATED } from "../core/constants";

// Just for reference - defined in store.js
const initialState = {
  bikes: [],
};

const deletedBikeIds = [];

const bike = (state = initialState, action) => {
  switch (action.type) {
    case BIKE_CREATED: {
      const bike = action.payload;
      return { ...state, bikes: [...state.bikes, bike] };
    }

    case BIKE_UPDATED: {
      const bike = action.payload;

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

    case BIKE_KILLED: {
      const { id } = action.payload;
      const newBikes = state.bikes.filter(c => c.id !== id);
      deletedBikeIds.push(id);

      return { ...state, bikes: newBikes };
    }

    default:
      return state;
  }
};

export default bike;
