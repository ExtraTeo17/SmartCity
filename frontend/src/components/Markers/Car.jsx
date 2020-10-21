import React from "react";
import { Marker, Popup } from "react-leaflet";
import { carIcon, testCarIcon } from "../../styles/icons";

const Car = props => {
  const {
    car: { id, location, isTestCar },
  } = props;

  return (
    <Marker position={location} icon={isTestCar ? testCarIcon : carIcon} zIndexOffset={20}>
      <Popup>I am a car-{id}!</Popup>
    </Marker>
  );
};

export default React.memo(Car);
