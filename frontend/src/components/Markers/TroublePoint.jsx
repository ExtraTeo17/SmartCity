import React from "react";
import { troublePointIcon } from "../../styles/icons";
import { Marker, Popup } from "react-leaflet";

const TroublePoint = props => {
  const { troublePoint } = props;

  return (
    <Marker position={troublePoint.location} opacity={0.95} icon={troublePointIcon} zIndexOffset={10}>
      <Popup>I am a troublePoint!</Popup>
    </Marker>
  );
};

export default React.memo(TroublePoint);
