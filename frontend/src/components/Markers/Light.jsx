import React from "react";
import { Marker, Popup } from "react-leaflet";
import { greenLightIcon, redLightIcon } from "../../styles/icons";
import { LightColor } from "../Models/LightColor";
import "../../styles/Light.css";

const Light = props => {
  const { light } = props;
  const initMarker = ref => {
    if (ref && light.jammed) {
      const elem = ref.leafletElement;
      const htmlElem = elem.getPane();
      htmlElem.classList.add("jammed");
      console.log(htmlElem);
    }
  };

  return (
    <Marker
      ref={initMarker}
      position={light.location}
      icon={light.color === LightColor.GREEN ? greenLightIcon : redLightIcon}
      zIndexOffset={10}
    >
      <Popup>I am a light-{light.id}!</Popup>
    </Marker>
  );
};

export default React.memo(Light);
