import React from "react";
import { Marker, Popup } from "react-leaflet";
import { STATIC_Z_INDEX } from "../../constants/markers";
import { troublePointIcon, accidentIcon } from "../../styles/icons";

const TroublePoint = props => {
  const { troublePoint, useFixed } = props;
  const icon = useFixed || troublePoint.isConstruction ? troublePointIcon : accidentIcon;

  return (
    <Marker position={troublePoint.location} opacity={0.95} icon={icon} zIndexOffset={STATIC_Z_INDEX}>
      <Popup>I am a troublePoint!</Popup>
    </Marker>
  );
};

export default React.memo(TroublePoint);
