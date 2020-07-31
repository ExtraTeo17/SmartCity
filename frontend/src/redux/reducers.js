import { CENTER_UPDATED } from "./constants";

export const initialState = {
  center: { lat: 52.23682, lng: 21.01681, rad: 200 },
};

// Read this: https://redux.js.org/basics/reducers
// https://redux.js.org/tutorials/essentials/part-1-overview-concepts

const appReducer = (state = initialState, action) => {
  switch (action.type) {
    case CENTER_UPDATED: {
      const { center } = action.payload;
      // CAREFUL: You can't modify state variable directly.
      return { ...state, center };
    }

    default:
      return state;
  }
};

export default appReducer;
