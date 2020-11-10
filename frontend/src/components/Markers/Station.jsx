import React from "react";
import { Marker, Popup } from "react-leaflet";
import { stationIcon } from "../../styles/icons";

const Station = props => {
  const { station } = props;

  return (
    <Marker position={station.location} opacity={0.95} icon={stationIcon} zIndexOffset={10}>
      <Popup>I am a station!</Popup>
    </Marker>
  );
};

export default React.memo(Station);
