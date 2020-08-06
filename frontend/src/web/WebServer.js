import { SERVER_ADDRESS, RECONNECT_INTERVAL_SEC } from "../utils/constants";
import MessageHandler from "./MessageHandler";

var socketContainer = {
  socket: {},
};

const createSocket = () => {
  const socket = new WebSocket(SERVER_ADDRESS);
  socket.onopen = () => {
    console.log("Connected !!!");
  };

  /**
   * @param {{ data: object; }} e
   */
  socket.onmessage = e => {
    console.group("OnMessage");

    console.log("Message received:" + e.data);
    let msgDto = JSON.parse(e.data);
    let msg = { type: msgDto.type, payload: JSON.parse(msgDto.payload) };
    MessageHandler.handle(msg);

    console.groupEnd();
  };

  socket.onerror = err => {
    console.error("Socket encountered error: ", err, "Closing socket");
    socket.close();
  };

  socket.onclose = e => {
    console.warn(`Socket is closed. Reconnect will be attempted in ${RECONNECT_INTERVAL_SEC} seconds`, e.reason);
    setTimeout(() => {
      socketContainer.socket = createSocket();
    }, RECONNECT_INTERVAL_SEC * 1000);
  };

  return socket;
};

/** PUBLIC INTERFACE ---------------------------------------------------------- */

socketContainer.socket = createSocket();

export default {
  /**
   * @param {{ type: number; payload: object; }} msgObj
   */
  send(msgObj) {
    console.group("Send");

    var msg = {
      type: msgObj.type,
      payload: JSON.stringify(msgObj.payload),
    };

    console.log(msg.payload);
    socketContainer.socket.send(JSON.stringify(msg));

    console.groupEnd();
  },
};
