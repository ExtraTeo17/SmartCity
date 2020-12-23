import React from "react";
import { Marker, Popup } from "react-leaflet";
import { greenLightIcon, redLightIcon } from "../../styles/icons";
import { LightColor } from "../Models/LightColor";
import { STATIC_Z_INDEX } from "../../constants/markers";

import "../../styles/Light.css";

const Light = props => {
  const {
    light: { id, jammed, location, color },
  } = props;

  function initMarker(ref) {
    if (ref && jammed) {
      const elem = ref.leafletElement;
      const htmlElem = elem.getElement();
      htmlElem.classList.add("jammed");
    } else if (ref) {
      const elem = ref.leafletElement;
      const htmlElem = elem.getElement();
      htmlElem.classList.remove("jammed");
    }
  }

  return (
    <Marker
      ref={initMarker}
      position={location}
      icon={color === LightColor.GREEN ? greenLightIcon : redLightIcon}
      zIndexOffset={STATIC_Z_INDEX}
    >
      <Popup>I am a light-{id}!</Popup>
    </Marker>
  );
};

export default React.memo(Light);
