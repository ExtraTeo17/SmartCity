import { notify } from "react-notify-toast";
import {
  PREPARE_SIMULATION_RESPONSE,
  CREATE_CAR_INFO,
  UPDATE_CAR_INFO,
  START_SIMULATION_RESPONSE,
  KILL_CAR_INFO,
  SWITCH_LIGHTS_INFO,
  CREATE_TROUBLE_POINT_INFO,
  HIDE_TROUBLE_POINT_INFO,
  START_TRAFFIC_JAM_INFO,
  END_TRAFFIC_JAM_INFO,
  UPDATE_CAR_ROUTE_INFO,
  UPDATE_BUS_INFO,
  UPDATE_BUS_FILL_STATE_INFO,
  KILL_BUS_INFO,
  CREATE_PEDESTRIAN_INFO,
  UPDATE_PEDESTRIAN_INFO,
  PUSH_PEDESTRIAN_INTO_BUS_INFO,
  PULL_PEDESTRIAN_FROM_BUS_INFO,
  KILL_PEDESTRIAN_INFO,
  CREATE_BIKE_INFO,
  UPDATE_BIKE_INFO,
  KILL_BIKE_INFO,
  BATCHED_UPDATE_INFO,
  CRASH_BUS_INFO,
} from "./MessageType";
import { NOTIFY_SHOW_MS } from "../constants/global";
import Dispatcher from "../redux/Dispatcher";
import { BusFillState } from "../components/Models/BusFillState";

/**
 * Handles all incoming messages
 * @category Web
 * @module MessageHandler
 */

/**
 * Helper function for logging messages
 * @param {Object} payload
 */
// eslint-disable-next-line no-unused-vars
function logMessage(payload) {
  console.groupCollapsed("HandledMessage");
  console.info("Message received.");
  console.info(payload);
  console.groupEnd();
}

export default {
  /**
   * Handles message by invoking appropriate Dispatcher method
   * @param {module:WebServer~Message} msg - Message to handle
   */
  handle(msg) {
    const { payload } = msg;
    switch (msg.type) {
      case PREPARE_SIMULATION_RESPONSE: {
        console.groupCollapsed("Prepared");
        console.info(msg.payload);
        console.groupEnd();
        const { lights, stations, buses } = payload;
        Dispatcher.prepareSimulation(lights, stations, buses);
        break;
      }

      case START_SIMULATION_RESPONSE:
        notify.show("Simulation started!", "success", NOTIFY_SHOW_MS);

        Dispatcher.startSimulation();
        break;

      case CREATE_CAR_INFO:
        Dispatcher.createCar(payload);
        break;

      case UPDATE_CAR_INFO:
        Dispatcher.updateCar(payload);
        break;

      case KILL_CAR_INFO:
        Dispatcher.killCar(payload);
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

      case HIDE_TROUBLE_POINT_INFO: {
        Dispatcher.hideTroublePoint(payload.id);
        break;
      }

      case START_TRAFFIC_JAM_INFO: {
        Dispatcher.startTrafficJam(payload.lightId);
        break;
      }

      case END_TRAFFIC_JAM_INFO: {
        Dispatcher.endTrafficJam(payload.lightId);
        break;
      }

      case UPDATE_BUS_INFO: {
        Dispatcher.updateBus(payload);
        break;
      }

      case UPDATE_BUS_FILL_STATE_INFO: {
        if (payload.fillState === BusFillState.HIGH) {
          notify.show(`Bus-${payload.id} overloaded!`, "warning", NOTIFY_SHOW_MS);
        }
        Dispatcher.updateBusFillState(payload);
        break;
      }

      case KILL_BUS_INFO: {
        Dispatcher.killBus(payload.id);
        break;
      }

      case CRASH_BUS_INFO: {
        Dispatcher.crashBus(payload.id);
        break;
      }

      case CREATE_PEDESTRIAN_INFO: {
        Dispatcher.createPedestrian(payload);
        break;
      }

      case UPDATE_PEDESTRIAN_INFO: {
        Dispatcher.updatePedestrian(payload);
        break;
      }

      case PUSH_PEDESTRIAN_INTO_BUS_INFO: {
        Dispatcher.pushPedestrianIntoBus(payload.id);
        break;
      }

      case PULL_PEDESTRIAN_FROM_BUS_INFO: {
        Dispatcher.pullPedestrianFromBus(payload);
        break;
      }

      case KILL_PEDESTRIAN_INFO: {
        Dispatcher.killPedestrian(payload);
        break;
      }

      case CREATE_BIKE_INFO:
        Dispatcher.createBike(payload);
        break;

      case UPDATE_BIKE_INFO:
        Dispatcher.updateBike(payload);
        break;

      case KILL_BIKE_INFO:
        Dispatcher.killBike(payload);
        break;

      case BATCHED_UPDATE_INFO:
        Dispatcher.updateBatched(payload.carUpdates, payload.bikeUpdates, payload.busUpdates, payload.pedUpdates);
        break;

      default:
        console.group("Unrecognized-message");
        console.warn(`Type: ${msg.type}`);
        console.info(payload);
        console.groupEnd();
    }
  },
};
