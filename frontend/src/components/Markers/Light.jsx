import React from "react";
import { greenLightIcon } from "../../styles/icons";
import { Marker, Popup } from "react-leaflet";

const Light = props => {
  const { location } = props;

  return (
    <Marker position={location} opacity={0.95} icon={greenLightIcon} zIndexOffset={10}>
      <Popup>I am a light!</Popup>
    </Marker>
  );
};

export default Light;
