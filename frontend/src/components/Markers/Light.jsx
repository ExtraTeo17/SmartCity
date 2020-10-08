import React from "react";
import { greenLightIcon, redLightIcon } from "../../styles/icons";
import { Marker, Popup } from "react-leaflet";
import { LightColor } from "../Models/LightColor";

const Light = props => {
  const { light } = props;
  console.log(light.color);

  return (
    <Marker
      position={light.location}
      opacity={0.95}
      icon={light.color === LightColor.GREEN ? greenLightIcon : redLightIcon}
      zIndexOffset={10}
    >
      <Popup>I am a light!</Popup>
    </Marker>
  );
};

export default Light;
