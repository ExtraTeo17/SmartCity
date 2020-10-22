import React from "react";
import { Marker, Popup } from "react-leaflet";
import { pedestrianIcon, testPedestrianIcon } from "../../styles/icons";

const Pedestrian = props => {
  const {
    pedestrian: { id, location, isTestPedestrian },
  } = props;

  return (
    <Marker
      position={location}
      icon={isTestPedestrian ? testPedestrianIcon : pedestrianIcon}
      zIndexOffset={isTestPedestrian ? 16 : 8}
    >
      <Popup>I am a pedestrian-{id}</Popup>
    </Marker>
  );
};

export default React.memo(Pedestrian);
