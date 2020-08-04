import { CENTER_UPDATED } from "../constants";

const initialState = {
  center: { lat: 52.23682, lng: 21.01681, rad: 200 },
};

const interaction = (state = initialState, action) => {
  switch (action.type) {
    case CENTER_UPDATED: {
      const { center } = action.payload;
      return { ...state, center };
    }

    default:
      return state;
  }
};

export default interaction;
