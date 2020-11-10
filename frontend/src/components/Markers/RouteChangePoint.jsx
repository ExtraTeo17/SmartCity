import React from "react";
import { Marker, Popup } from "react-leaflet";
import { STATIC_Z_INDEX } from "../../constants/markers";
import { goldMarkerIcon } from "../../styles/icons";

const RouteChangePoint = props => {
  const { point } = props;

  return point ? (
    <Marker position={point} icon={goldMarkerIcon} zIndexOffset={STATIC_Z_INDEX + 2}>
      <Popup>Route changed here!</Popup>
    </Marker>
  ) : null;
};

export default React.memo(RouteChangePoint);
