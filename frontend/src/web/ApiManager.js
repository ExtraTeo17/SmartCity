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

  startSimulation(
    { carsNum, testCarNum, generateCars, generateTroublePoints, time } = {
      carsNum: 0,
      testCarNum: 0,
      generateCars: true,
      generateTroublePoints: false,
      time: new Date(),
    }
  ) {
    const msg = {
      type: START_SIMULATION_REQUEST,
      payload: {
        carsNum,
        testCarId: testCarNum,
        generateCars,
        generateTroublePoints,
        startTime: time,
      },
    };
    WebServer.send(msg);
  },
};
