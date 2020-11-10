import React from "react";
import { Marker, Popup } from "react-leaflet";
import { greenLightIcon, redLightIcon } from "../../styles/icons";
import { LightColor } from "../Models/LightColor";
import "../../styles/Light.css";
import { STATIC_Z_INDEX } from "../../constants/markers";

const Light = props => {
  const { light } = props;
  const initMarker = ref => {
    if (ref && light.jammed) {
      const elem = ref.leafletElement;
      const htmlElem = elem.getElement();
      htmlElem.classList.add("jammed");
    } else if (ref) {
      const elem = ref.leafletElement;
      const htmlElem = elem.getElement();
      htmlElem.classList.remove("jammed");
    }
  };

  return (
    <Marker
      ref={initMarker}
      position={light.location}
      icon={light.color === LightColor.GREEN ? greenLightIcon : redLightIcon}
      zIndexOffset={STATIC_Z_INDEX}
    >
      <Popup>I am a light-{light.id}!</Popup>
    </Marker>
  );
};

export default React.memo(Light);
