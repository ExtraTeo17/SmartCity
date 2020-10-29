import React from "react";
import { Marker, Popup } from "react-leaflet";
import { bikeIcon, testBikeIcon } from "../../styles/icons";

const Bike = props => {
  const {
    bike: { id, location, isTestBike },
  } = props;

  return (
    <Marker position={location} icon={isTestBike ? testBikeIcon : bikeIcon} zIndexOffset={20}>
      <Popup>I am a bike-{id}!</Popup>
    </Marker>
  );
};

export default React.memo(Bike);
