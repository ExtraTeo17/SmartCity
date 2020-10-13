import {
  PREPARE_SIMULATION_RESPONSE,
  CREATE_CAR_INFO,
  UPDATE_CAR_INFO,
  START_SIMULATION_RESPONSE,
  KILL_CAR_INFO,
} from "./MessageType";
import { NOTIFY_SHOW_SEC } from "../utils/constants";
import { notify } from "react-notify-toast";
import Dispatcher from "../redux/Dispatcher";

export default {
  handle(msg) {
    const payload = msg.payload;
    switch (msg.type) {
      case PREPARE_SIMULATION_RESPONSE:
        notify.show("Simulation prepared!", "success", NOTIFY_SHOW_SEC * 1000);

        Dispatcher.prepareSimulation(payload.locations);
        break;

      case START_SIMULATION_RESPONSE:
        notify.show("Simulation started!", "success", NOTIFY_SHOW_SEC * 1000);

        Dispatcher.startSimulation(msg.payload.timeScale);
        break;

      case CREATE_CAR_INFO:
        Dispatcher.createCar(payload);
        break;

      case UPDATE_CAR_INFO:
        Dispatcher.updateCar(payload);
        break;

      case KILL_CAR_INFO:
        Dispatcher.killCar(payload.id);
        break;

      default:
        console.warn("Unrecognized message type");
    }
  },
};
