import React from "react";
import { carIcon } from "../../styles/icons";
import { Marker, Popup } from "react-leaflet";

const Car = props => {
  const { id, location } = props;

  return (
    <Marker position={location} opacity={0.95} icon={carIcon}>
      <Popup>I am a car!</Popup>
    </Marker>
  );
};

export default Car;
