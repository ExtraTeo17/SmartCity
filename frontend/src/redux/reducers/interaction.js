import { CENTER_UPDATED } from "../constants";

// Just for reference - defined in store.js
const initialState = {
  center: { lat: 0, lng: 0, rad: 0 },
};

/**
 * @param {{ type: any; payload: { center: { lat: number; lng: number; rad: number}; }; }} action
 */
const interaction = (state = initialState, action) => {
  switch (action.type) {
    case CENTER_UPDATED: {
      const center = action.payload;
      return { ...state, center };
    }

    default:
      return state;
  }
};

export default interaction;
