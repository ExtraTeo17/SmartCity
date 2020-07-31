import { SERVER_ADDRESS } from "../utils/constants";
import { SET_ZONE } from "./MessageType";

var socket = new WebSocket(SERVER_ADDRESS);
socket.onopen = () => {
  console.log("Connected !!!");
};

export default {
  setZone({ lat, lng, rad } = { lat: 0, lng: 0, rad: 0 }) {
    var msg = {
      type: SET_ZONE,
      payload: {
        latitude: lat,
        longitude: lng,
        radius: rad,
      },
    };
    this._send(msg);
  },

  /**
   * @param {{ type: any; payload: any; }} msgObj
   */
  _send(msgObj) {
    var msg = {
      type: msgObj.type,
      payload: JSON.stringify(msgObj.payload),
    };

    console.log(msg.payload);
    socket.send(JSON.stringify(msg));
  },
};
