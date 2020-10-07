import { PREPARE_SIMULATION_RESPONSE, CREATE_CAR_INFO, UPDATE_CAR_INFO, START_SIMULATION_RESPONSE } from "./MessageType";
import { NOTIFY_SHOW_SEC } from "../utils/constants";
import { dispatch } from "../redux/store";
import { carUpdated, carCreated, lightsCreated } from "../redux/actions";
import { batch } from "react-redux";
import { notify } from "react-notify-toast";

const fps = 20;
let timeScale = 1;
let timer;
let carUpdateQueue = [];

export default {
  handle(msg) {
    const payload = msg.payload;
    switch (msg.type) {
      case PREPARE_SIMULATION_RESPONSE:
        notify.show("Simulation prepared!", "success", NOTIFY_SHOW_SEC * 1000);

        const lights = payload.lights;
        dispatch(lightsCreated(lights));
        break;

      case START_SIMULATION_RESPONSE:
        notify.show("Simulation started!", "success", NOTIFY_SHOW_SEC * 1000);

        timeScale = msg.payload.timeScale;
        timer = setInterval(() => {
          batch(() => {
            carUpdateQueue.forEach(action => dispatch(action));
          });
          carUpdateQueue = [];
        }, 1000 / fps);
        break;

      case CREATE_CAR_INFO: {
        const car = payload;
        dispatch(carCreated(car));
        break;
      }

      case UPDATE_CAR_INFO: {
        const car = payload;
        carUpdateQueue.push(carUpdated(car));
        break;
      }

      default:
        console.warn("Unrecognized message type");
    }
  },
};
