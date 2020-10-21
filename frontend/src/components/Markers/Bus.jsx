import React from "react";
import { Marker, Popup } from "react-leaflet";
import { busLowIcon, busMidIcon, busHighIcon } from "../../styles/icons";
import { BusFillState } from "../Models/BusFillState";

const Bus = props => {
  const {
    bus: { id, location, fillState },
  } = props;

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
    <Marker position={location} icon={getIcon()} zIndexOffset={20}>
      <Popup>I am a bus-{id}!</Popup>
    </Marker>
  );
};

export default React.memo(Bus);
