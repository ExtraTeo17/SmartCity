import React from "react";
import { Marker, Popup } from "react-leaflet";
import { greenLightIcon, redLightIcon } from "../../styles/icons";
import { LightColor } from "../Models/LightColor";

const Light = props => {
  const { light } = props;

  return (
    <Marker position={light.location} icon={light.color === LightColor.GREEN ? greenLightIcon : redLightIcon} zIndexOffset={10}>
      <Popup>I am a light!</Popup>
    </Marker>
  );
};

export default React.memo(Light);
