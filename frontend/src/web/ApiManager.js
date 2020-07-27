import { SERVER_ADDRESS } from "../utils/constants";
import { SET_ZONE } from "./MessageType";

// TODO: static?
class ApiManager {
  constructor() {
    this.socket = new WebSocket(SERVER_ADDRESS);
    this.socket.onopen = e => {
      console.log("Connected !!!");
    };
  }

  setZone() {
    // TODO: payload
    var msg = JSON.stringify({
      type: SET_ZONE,
      payload: "",
    });
    this.socket.send(msg);
  }
}

export default ApiManager;
