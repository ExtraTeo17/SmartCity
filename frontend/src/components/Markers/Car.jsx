import React from "react";
import { carIcon, testCarIcon } from "../../styles/icons";
import { Marker, Popup } from "react-leaflet";

const Car = props => {
  const { id, location, isTestCar } = props.car;

  return (
    <Marker position={location} opacity={0.95} icon={isTestCar ? testCarIcon : carIcon} zIndexOffset={20}>
      <Popup>I am a car-{id}!</Popup>
    </Marker>
  );
};

export default Car;
