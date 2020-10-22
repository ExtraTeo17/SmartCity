import React from "react";
import { Marker, Popup } from "react-leaflet";
import { troublePointIcon } from "../../styles/icons";

const TroublePoint = props => {
  const { troublePoint } = props;

  return (
    <Marker position={troublePoint.location} opacity={0.95} icon={troublePointIcon} zIndexOffset={10}>
      <Popup>I am a troublePoint!</Popup>
    </Marker>
  );
};

export default React.memo(TroublePoint);
