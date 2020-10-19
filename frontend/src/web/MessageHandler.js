import {
  PREPARE_SIMULATION_RESPONSE,
  CREATE_CAR_INFO,
  UPDATE_CAR_INFO,
  START_SIMULATION_RESPONSE,
  KILL_CAR_INFO,
  SWITCH_LIGHTS_INFO,
  CREATE_TROUBLE_POINT_INFO,
  UPDATE_CAR_ROUTE_INFO,
  BUS_UPDATED,
  BUS_FILL_STATE_UPDATED,
} from "./MessageType";
import { NOTIFY_SHOW_MS } from "../utils/constants";
import { notify } from "react-notify-toast";
import Dispatcher from "../redux/Dispatcher";

export default {
  handle(msg) {
    const payload = msg.payload;
    switch (msg.type) {
      case PREPARE_SIMULATION_RESPONSE: {
        notify.show("Simulation prepared!", "success", NOTIFY_SHOW_MS);
        console.groupCollapsed("Prepared");
        console.log(msg.payload);
        console.groupEnd();
        const { lights, stations, buses } = payload;
        Dispatcher.prepareSimulation(lights, stations, buses);
        break;
      }

      case START_SIMULATION_RESPONSE:
        notify.show("Simulation started!", "success", NOTIFY_SHOW_MS);

        Dispatcher.startSimulation(payload.timeScale);
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

      case UPDATE_CAR_ROUTE_INFO:
        Dispatcher.updateCarRoute(payload);
        break;

      case SWITCH_LIGHTS_INFO: {
        Dispatcher.switchLights(payload.lightGroupId);
        break;
      }

      case CREATE_TROUBLE_POINT_INFO: {
        Dispatcher.createTroublePoint(payload);
        break;
      }

      case BUS_UPDATED: {
        Dispatcher.updateBus(payload);
        break;
      }

      case BUS_FILL_STATE_UPDATED: {
        Dispatcher.updateBusFillState(payload);
        break;
      }

      default:
        console.group("Unrecognized message");
        console.warn("Type: " + msg.type);
        console.info(payload);
        console.groupEnd();
    }
  },
};
