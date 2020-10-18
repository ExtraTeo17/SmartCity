import { SERVER_ADDRESS, RECONNECT_INTERVAL_SEC, NOTIFY_SHOW_MS } from "../utils/constants";
import MessageHandler from "./MessageHandler";
import { notify } from "react-notify-toast";

var socketContainer = {
  socket: {},
};

const createSocket = () => {
  const socket = new WebSocket(SERVER_ADDRESS);
  socket.onopen = () => {
    console.info("Connected !!!");

    notify.show("Sucessfully connected", "success", NOTIFY_SHOW_MS);
  };

  /**
   * @param {{ data: object; }} e
   */
  socket.onmessage = e => {
    logMessage(e);
    let msgDto = JSON.parse(e.data);
    let msg = { type: msgDto.type, payload: JSON.parse(msgDto.payload) };
    MessageHandler.handle(msg);
  };

  function logMessage(e) {
    console.groupCollapsed("OnMessage");
    console.log("Message received:" + e.data);
    console.groupEnd();
  }

  socket.onerror = err => {
    console.error("Socket encountered error: ", err, "Closing socket");
    socket.close();
  };

  socket.onclose = e => {
    console.warn(`Socket is closed. Reconnect will be attempted in ${RECONNECT_INTERVAL_SEC} seconds`, e.reason);
    notify.show("Error encountered, trying to reconnect...", "error", RECONNECT_INTERVAL_SEC * 1000);
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
