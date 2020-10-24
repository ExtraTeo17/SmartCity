import { PREPARE_SIMULATION_REQUEST, START_SIMULATION_REQUEST } from "./MessageType";
import WebServer from "./WebServer";

/** PUBLIC INTERFACE ---------------------------------------------------------- */

export default {
  prepareSimulation({ lat, lng, rad, generatePedestrians } = { lat: 0, lng: 0, rad: 0 }) {
    const msg = {
      type: PREPARE_SIMULATION_REQUEST,
      payload: {
        latitude: lat,
        longitude: lng,
        radius: rad,
        generatePedestrians,
      },
    };
    WebServer.send(msg);
  },

  startSimulation({ carsNum, testCarNum, generateCars, generateTroublePoints } = { carsNum: 0, testCarNum: 0 }) {
    const msg = {
      type: START_SIMULATION_REQUEST,
      payload: {
        carsNum,
        testCarId: testCarNum,
        generateCars,
        generateTroublePoints,
      },
    };
    WebServer.send(msg);
  },
};
