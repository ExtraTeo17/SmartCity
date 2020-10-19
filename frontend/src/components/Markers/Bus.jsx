import React from "react";
import { busLowIcon, busMidIcon, busHighIcon } from "../../styles/icons";
import { Marker, Popup } from "react-leaflet";
import { BusFillState } from "../Models/BusFillState";

const Bus = props => {
  const { id, location, fillState } = props.bus;

  function getIcon() {
    switch (fillState) {
      case BusFillState.LOW:
        return busLowIcon;
      case BusFillState.MID:
        return busMidIcon;
      case BusFillState.HIGH:
        console.log("High!");
        return busHighIcon;

      default:
        console.warn("Unrecognized fillState: " + fillState);
        return busLowIcon;
    }
  }

  return (
    <Marker position={location} icon={getIcon()} zIndexOffset={20}>
      <Popup>I am a bus-{id}!</Popup>
    </Marker>
  );
};

export default Bus;
