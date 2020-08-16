import { SET_ZONE_REQUEST } from "./MessageType";
import WebServer from "./WebServer";

/** PUBLIC INTERFACE ---------------------------------------------------------- */

export default {
  setZone({ lat, lng, rad } = { lat: 0, lng: 0, rad: 0 }) {
    var msg = {
      type: SET_ZONE_REQUEST,
      payload: {
        latitude: lat,
        longitude: lng,
        radius: rad,
      },
    };
    WebServer.send(msg);
  },
};
