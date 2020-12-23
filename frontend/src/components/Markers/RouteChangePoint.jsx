import React from "react";
import { Popup, CircleMarker } from "react-leaflet";
import { STATIC_Z_INDEX } from "../../constants/markers";

const RouteChangePoint = props => {
  const { point } = props;

  return point ? (
    <CircleMarker
      center={point}
      radius={8}
      color="yellow"
      opacity={0.75}
      fillColor="yellow"
      fillOpacity={0.85}
      zIndexOffset={STATIC_Z_INDEX + 2}
    >
      <Popup>Route changed here!</Popup>
    </CircleMarker>
  ) : null;
};

export default React.memo(RouteChangePoint);
