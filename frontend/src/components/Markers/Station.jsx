import React from "react";
import { Marker, Popup } from "react-leaflet";
import { STATIC_Z_INDEX } from "../../constants/markers";
import { stationIcon } from "../../styles/icons";

const Station = props => {
  const { station } = props;

  return (
    <Marker position={station.location} opacity={0.95} icon={stationIcon} zIndexOffset={STATIC_Z_INDEX}>
      <Popup>I am a station-{station.id}!</Popup>
    </Marker>
  );
};

export default React.memo(Station);
