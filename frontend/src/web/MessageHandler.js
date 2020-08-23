import { PREPARE_SIMULATION_RESPONSE } from "./MessageType";
import { dispatch } from "../redux/store";
import { lightLocationsUpdated } from "../redux/actions";

export default {
  handle(msg) {
    const payload = msg.payload;
    switch (msg.type) {
      case PREPARE_SIMULATION_RESPONSE:
        const locations = payload.locations;
        dispatch(lightLocationsUpdated(locations));
        break;

      default:
        console.warn("Unrecognized message type");
    }
  },
};
