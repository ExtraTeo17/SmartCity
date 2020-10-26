import { CENTER_UPDATED, START_SIMULATION_DATA_UPDATED } from "../constants";

// Just for reference - defined in store.js
const initialState = {
  center: { lat: 0, lng: 0, rad: 0 },
  startSimulationData: {
    carsNum: 0,
    testCarNum: 0,
    generateCars: true,
    generateTroublePoints: false,
    timeBeforeTrouble: 5,
    time: new Date(),
  },
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

    case START_SIMULATION_DATA_UPDATED: {
      const data = action.payload;
      return { ...state, startSimulationData: { ...state.startSimulationData, ...data } };
    }

    default:
      return state;
  }
};

export default interaction;
