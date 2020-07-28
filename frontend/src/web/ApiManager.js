import { SERVER_ADDRESS } from "../utils/constants";
import { SET_ZONE } from "./MessageType";

// TODO: static?
class ApiManager {
  constructor() {
    this.socket = new WebSocket(SERVER_ADDRESS);
    // TODO: Why connected twice?
    this.socket.onopen = e => {
      console.log("Connected !!!");
    };
  }

  setZone({ latitude, longitude, radius } = {}) {
    var msg = {
      type: SET_ZONE,
      payload: {
        latitude,
        longitude,
        radius,
      },
    };
    this._send(msg);
  }

  _send(msgObj) {
    var msg = {
      type: msgObj.type,
      payload: JSON.stringify(msgObj.payload),
    };

    console.log(msg.payload);
    this.socket.send(JSON.stringify(msg));
  }
}

export default ApiManager;
