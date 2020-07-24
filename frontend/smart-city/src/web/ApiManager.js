import { SERVER_ADDRESS } from "../utils/constants";

class ApiManager {
  constructor() {
    let socket = new WebSocket(SERVER_ADDRESS);
    socket.onopen = e => {
      console.log("Connected !!!");
    };
  }
}

export default ApiManager;
