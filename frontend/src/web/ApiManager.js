import { SET_ZONE_REQUEST, START_SIMULATION_REQUEST } from "./MessageType";
import WebServer from "./WebServer";

/** PUBLIC INTERFACE ---------------------------------------------------------- */

export default {
  setZone({ lat, lng, rad } = { lat: 0, lng: 0, rad: 0 }) {
    const msg = {
      type: SET_ZONE_REQUEST,
      payload: {
        latitude: lat,
        longitude: lng,
        radius: rad,
      },
    };
    WebServer.send(msg);
  },
  startVehicles({ carsNum, testCarNum } = { carsNum: 0, testCarNum: 0 }) {
    const msg = {
      type: START_SIMULATION_REQUEST,
      payload: {
        carsNum: carsNum,
        testCarId: testCarNum,
      },
    };
    WebServer.send(msg);
  },
};
