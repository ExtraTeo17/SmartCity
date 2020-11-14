import React, { useEffect, useReducer } from "react";
import { Popup } from "react-leaflet";
import { BUS_ROTATION_THRESHOLD, MOVING_Z_INDEX } from "../../constants/markers";
import { busLowIcon, busMidIcon, busHighIcon } from "../../styles/icons";
import { BusFillState } from "../Models/BusFillState";
import { angleFromCoordinates } from "../../utils/helpers";
import { getRotationReducer } from "./Extensions/reducers";
import RotatedMarker from "./Extensions/RotatedMarker";

const Bus = props => {
  const {
    bus: { id, route, location, fillState },
  } = props;

  const defaultAngle = route ? angleFromCoordinates(route[0], route[1]) : 0;
  const [state, dispatch] = useReducer(getRotationReducer(BUS_ROTATION_THRESHOLD), { loc: location, angle: defaultAngle });

  useEffect(() => {
    dispatch({ payload: location });
  }, [location]);

  function getIcon() {
    switch (fillState) {
      case BusFillState.LOW:
        return busLowIcon;
      case BusFillState.MID:
        return busMidIcon;
      case BusFillState.HIGH:
        return busHighIcon;

      default:
        console.warn(`Unrecognized fillState: ${fillState}`);
        return busLowIcon;
    }
  }

  return (
    <RotatedMarker
      rotationAngle={state.angle}
      rotationOrigin="center"
      position={state.loc}
      icon={getIcon()}
      zIndexOffset={MOVING_Z_INDEX}
    >
      <Popup>I am a bus-{id}!</Popup>
    </RotatedMarker>
  );
};

export default React.memo(Bus);
