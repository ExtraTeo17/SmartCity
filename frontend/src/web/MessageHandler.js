import { PREPARE_SIMULATION_RESPONSE, CREATE_CAR_INFO } from "./MessageType";
import { dispatch } from "../redux/store";
import { carCreated, lightLocationsUpdated } from "../redux/actions";

export default {
  handle(msg) {
    const payload = msg.payload;
    switch (msg.type) {
      case PREPARE_SIMULATION_RESPONSE:
        const locations = payload.locations;
        dispatch(lightLocationsUpdated(locations));
        break;

      case CREATE_CAR_INFO:
        const location = payload.location;
        dispatch(carCreated(location));
        break;

      default:
        console.warn("Unrecognized message type");
    }
  },
};
