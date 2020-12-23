import { notify } from "react-notify-toast";
import { SERVER_ADDRESS, RECONNECT_INTERVAL_SEC, NOTIFY_SHOW_MS } from "../constants/global";
import MessageHandler from "./MessageHandler";

/**
 * Used for interaction with web sockets.
 * @category Web
 * @module WebServer
 */

/**
 * @typedef {Object} Message - object used for communication with sever
 * @property {module:MessageType~MessageType} type
 * @property {Object} payload - message-specific data
 */

const socketContainer = {
  socket: {},
  reconnecting: false,
  connected: false,
};

/**
 * Initializes and returns WebSocket with automatic reconnection
 * @returns {WebSocket} Opened socket
 */
function createSocket() {
  const socket = new WebSocket(SERVER_ADDRESS);
  socket.onopen = () => {
    console.info("Connected !!!");
    socketContainer.connected = true;
    socketContainer.reconnecting = false;
    notify.hide();
    notify.show("Sucessfully connected", "success", NOTIFY_SHOW_MS);
  };

  /**
   * @param {{ data: Object }} e
   */
  socket.onmessage = e => {
    const msgDto = JSON.parse(e.data);
    const msg = { type: msgDto.type, payload: JSON.parse(msgDto.payload) };
    MessageHandler.handle(msg);
  };

  // eslint-disable-next-line
  function logMessage(e) {
    console.groupCollapsed("OnMessage");
    console.info(`Message received:${e.data}`);
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

    socketContainer.connected = false;
    setTimeout(() => {
      socketContainer.reconnecting = true;
      socketContainer.socket = createSocket();
    }, RECONNECT_INTERVAL_SEC * 1000);
  };

  return socket;
}

socketContainer.socket = createSocket();

export default {
  /**
   * Sends stringified JSON message to connected server
   * @param {Message} msgObj
   */
  send(msgObj) {
    if (socketContainer.socket.readyState !== WebSocket.OPEN) {
      notify.show("Please wait for connection", "warning", NOTIFY_SHOW_MS / 2);
      return;
    }

    console.group("Send");
    const msg = {
      type: msgObj.type,
      payload: JSON.stringify(msgObj.payload),
    };

    console.info(msg.payload);
    socketContainer.socket.send(JSON.stringify(msg));

    console.groupEnd();
  },

  /**
   * Returns state of connection
   * @returns {Boolean} True if connected, false otherwise
   */
  isConnected() {
    return socketContainer.connected;
  },
};
