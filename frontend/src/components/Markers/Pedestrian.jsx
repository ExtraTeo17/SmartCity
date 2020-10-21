import React from "react";
import { Marker } from "react-leaflet";
import { pedestrianIcon, testPedestrianIcon } from "../../styles/icons";

const Pedestrian = props => {
  const {
    pedestrian: { location, isTestPedestrian },
  } = props;

  return <Marker position={location} icon={isTestPedestrian ? testPedestrianIcon : pedestrianIcon} zIndexOffset={15} />;
};

export default React.memo(Pedestrian);
