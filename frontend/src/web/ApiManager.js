import { SERVER_ADDRESS } from "../utils/constants";
import { SET_ZONE_REQUEST } from "./MessageType";
import MessageHandler from "./MessageHandler";

var socket = new WebSocket(SERVER_ADDRESS);
socket.onopen = () => {
  console.log("Connected !!!");
};

/**
 * @param {{ data: object; }} e
 */
socket.onmessage = e => {
  console.log("Message received:" + e.data);
  let msgDto = JSON.parse(e.data);
  let msg = { type: msgDto.type, payload: JSON.parse(msgDto.payload) };
  MessageHandler.handle(msg);
};

/** HELPERS ---------------------------------------------------------- */

/**
 * @param {{ type: number; payload: object; }} msgObj
 */
const send = msgObj => {
  var msg = {
    type: msgObj.type,
    payload: JSON.stringify(msgObj.payload),
  };

  console.log(msg.payload);
  socket.send(JSON.stringify(msg));
};

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
    send(msg);
  },
};
