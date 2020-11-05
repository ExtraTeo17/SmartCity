import React, { useEffect, useState } from "react";
import { Marker, Popup } from "react-leaflet";
import { carIcon, testCarIcon } from "../../styles/icons";
import { usePrevious } from "../../utils/helpers";
import RotatedMarker from "./RotatedMarker";

let i = 0;
const Car = props => {
  const {
    car: { id, location, isTestCar },
  } = props;
  const prevLocation = usePrevious(location);
  const [loc, setLoc] = useState(location);
  useEffect(() => {
    setLoc(location);
    ++i;
  }, [location]);

  const icon = isTestCar ? testCarIcon : carIcon;

  return (
    <RotatedMarker rotationAngle={i} position={loc} icon={icon} zIndexOffset={20} style={{ width: 100 }}>
      <Popup>I am a car-{id}!</Popup>
    </RotatedMarker>
  );
};

export default React.memo(Car);
