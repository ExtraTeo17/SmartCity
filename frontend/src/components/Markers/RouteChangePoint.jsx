import React from "react";
import { Marker, Popup } from "react-leaflet";
import { goldMarkerIcon } from "../../styles/icons";

const RouteChangePoint = props => {
  const { point } = props;

  return point ? (
    <Marker position={point} icon={goldMarkerIcon} zIndexOffset={12}>
      <Popup>Route changed here!</Popup>
    </Marker>
  ) : null;
};

export default RouteChangePoint;
