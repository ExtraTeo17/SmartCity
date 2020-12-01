import React, { useEffect, useReducer } from "react";
import { Popup } from "react-leaflet";
import { BUS_MOVING_Z_INDEX, BUS_ROTATION_THRESHOLD } from "../../constants/markers";
import { busLowIcon, busMidIcon, busHighIcon } from "../../styles/icons";
import { BusFillState } from "../Models/BusFillState";
import { angleFromCoordinates } from "../../utils/helpers";
import { getRotationReducer } from "./Extensions/reducers";
import RotatedMarker from "./Extensions/RotatedMarker";

import "../../styles/Bus.css";

const Bus = props => {
  const {
    bus: { id, route, location, fillState, crashed },
  } = props;

  const defaultAngle = route ? angleFromCoordinates(route[0], route[1]) : 0;
  const [state, dispatch] = useReducer(getRotationReducer(BUS_ROTATION_THRESHOLD), { loc: location, angle: defaultAngle });

  useEffect(() => {
    dispatch({ payload: location });
  }, [location]);

  const markerRef = React.useRef();

  useEffect(() => {}, [crashed]);

  function initMarker(ref) {
    if (ref) {
      markerRef.current = ref.leafletElement;
      if (crashed) {
        console.info("Crashed!");
        const htmlElem = markerRef.current.getElement();
        htmlElem.classList.add("crashed");
        // markerRef.current.openPopup();
      }
    }
  }

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

  const popup = crashed ? (
    <Popup
      offset={[0, 22]}
      closeOnClick={false}
      closeOnEscapeKey={false}
      closeButton={false}
      keepInView={false}
      className="trans-popup"
    >
      &#128369;
    </Popup>
  ) : (
    <Popup>I am a bus-{id}!</Popup>
  );

  return (
    <RotatedMarker
      customRef={initMarker}
      rotationAngle={state.angle}
      rotationOrigin="center"
      position={state.loc}
      icon={getIcon()}
      zIndexOffset={BUS_MOVING_Z_INDEX}
    >
      {popup}
    </RotatedMarker>
  );
};

export default React.memo(Bus);
