import { notify } from "react-notify-toast";
import { SERVER_ADDRESS, RECONNECT_INTERVAL_SEC, NOTIFY_SHOW_MS } from "../constants/global";
import MessageHandler from "./MessageHandler";

const socketContainer = {
  socket: {},
  reconnecting: false,
};

const createSocket = () => {
  const socket = new WebSocket(SERVER_ADDRESS);
  socket.onopen = () => {
    console.info("Connected !!!");
    socketContainer.reconnecting = false;
    notify.hide();
    notify.show("Sucessfully connected", "success", NOTIFY_SHOW_MS);
  };

  /**
   * @param {{ data: object; }} e
   */
  socket.onmessage = e => {
    // logMessage(e);
    const msgDto = JSON.parse(e.data);
    const msg = { type: msgDto.type, payload: JSON.parse(msgDto.payload) };
    MessageHandler.handle(msg);
  };

  function logMessage(e) {
    console.groupCollapsed("OnMessage");
    console.log(`Message received:${e.data}`);
    console.groupEnd();
  }

  socket.onerror = () => {
    if (!socketContainer.reconnecting) {
      console.error("Socket encountered error: ", "Closing socket");
    }
    socket.close();
  };

  socket.onclose = e => {
    if (!socketContainer.reconnecting) {
      console.warn(`Socket is closed. Reconnect will be attempted in ${RECONNECT_INTERVAL_SEC} seconds`, e.reason);
      notify.show("Error encountered, trying to reconnect...", "error", -1);
    }

    setTimeout(() => {
      socketContainer.reconnecting = true;
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

    const msg = {
      type: msgObj.type,
      payload: JSON.stringify(msgObj.payload),
    };

    console.log(msg.payload);
    socketContainer.socket.send(JSON.stringify(msg));

    console.groupEnd();
  },
};
