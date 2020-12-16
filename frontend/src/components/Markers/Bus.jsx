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

  const elemRef = React.useRef();
  function initMarker(ref) {
    if (ref) {
      elemRef.current = ref;
    }

    if (elemRef.current && crashed) {
      const elem = elemRef.current.leafletElement;
      const htmlElem = elem.getElement();
      htmlElem.classList.add("crashed");
      elem.openPopup();
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

  return (
    <RotatedMarker
      customRef={initMarker}
      rotationAngle={state.angle}
      rotationOrigin="center"
      position={state.loc}
      icon={getIcon()}
      zIndexOffset={BUS_MOVING_Z_INDEX}
    >
      {crashed && (
        <Popup
          offset={[-4, 21]}
          closeOnClick={false}
          closeOnEscapeKey={false}
          closeButton={false}
          keepInView={false}
          className="trans-popup"
        >
          &#128369;
        </Popup>
      )}
      {!crashed && <Popup>I am a bus-{id}!</Popup>}
    </RotatedMarker>
  );
};

export default React.memo(Bus);
