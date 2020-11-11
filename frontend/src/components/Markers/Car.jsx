import React, { useEffect, useReducer } from "react";
import { Popup } from "react-leaflet";
import { CAR_ROTATION_THRESHOLD, MOVING_Z_INDEX } from "../../constants/markers";
import { carIcon, testCarIcon } from "../../styles/icons";
import { angleFromCoordinates } from "../../utils/helpers";
import { getRotationReducer } from "./Extensions/reducers";
import RotatedMarker from "./Extensions/RotatedMarker";

const Car = props => {
  const {
    car: { id, route, location, isTestCar },
  } = props;

  const defaultAngle = route ? angleFromCoordinates(route[0], route[1]) : 0;
  const [state, dispatch] = useReducer(getRotationReducer(CAR_ROTATION_THRESHOLD), { loc: location, angle: defaultAngle });

  useEffect(() => {
    dispatch({ payload: location });
  }, [location]);

  const icon = isTestCar ? testCarIcon : carIcon;
  const zOffset = isTestCar ? MOVING_Z_INDEX + 1 : MOVING_Z_INDEX;

  return (
    <RotatedMarker rotationAngle={state.angle} rotationOrigin="center" position={state.loc} icon={icon} zIndexOffset={zOffset}>
      <Popup>I am a car-{id}!</Popup>
    </RotatedMarker>
  );
};

export default React.memo(Car);
