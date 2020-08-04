import { SET_ZONE_RESPONSE } from "./MessageType";
import { dispatch } from "../redux/store";
import { lightLocationsUpdated } from "../redux/actions";

export default {
  handle(msg) {
    const payload = msg.payload;
    switch (msg.type) {
      case SET_ZONE_RESPONSE:
        const locations = payload.locations;
        console.log("Locations");
        console.log(locations);
        dispatch(lightLocationsUpdated(locations));
        break;

      default:
        console.log("Unrecognized message type");
    }
  },
};
