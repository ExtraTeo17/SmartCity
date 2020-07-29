import { SERVER_ADDRESS } from "../utils/constants";
import { SET_ZONE } from "./MessageType";

var socket = new WebSocket(SERVER_ADDRESS);
socket.onopen = () => {
  console.log("Connected !!!");
};

// TODO: static?
export default {
  setZone({ latitude, longitude, radius } = { latitude: 0, longitude: 0, radius: 0 }) {
    var msg = {
      type: SET_ZONE,
      payload: {
        latitude,
        longitude,
        radius,
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
