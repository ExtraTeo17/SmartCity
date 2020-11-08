import React, { useEffect, useReducer } from "react";
import { Popup } from "react-leaflet";
import { CAR_ROTATION_THRESHOLD } from "../../constants/thresholds";
import { carIcon, testCarIcon } from "../../styles/icons";
import { angleFromCoordinates } from "../../utils/helpers";
import RotatedMarker from "./RotatedMarker";

function rotationReducer(state = { loc: 0, angle: 0 }, action) {
  const newLocation = action.payload;
  if (state.loc !== newLocation) {
    const newAngle = angleFromCoordinates(state.loc, newLocation);
    if (Math.abs(newAngle - state.angle) > CAR_ROTATION_THRESHOLD) {
      return { loc: newLocation, angle: newAngle };
    }

    return { ...state, loc: newLocation };
  }

  return state;
}

const Car = props => {
  const {
    car: { id, route, location, isTestCar },
  } = props;

  const defaultAngle = route ? angleFromCoordinates(route[0], route[1]) : 0;
  const [state, dispatch] = useReducer(rotationReducer, { loc: location, angle: defaultAngle });

  useEffect(() => {
    dispatch({ payload: location });
  }, [location]);

  const icon = isTestCar ? testCarIcon : carIcon;

  return (
    <RotatedMarker rotationAngle={state.angle} rotationOrigin="center" position={state.loc} icon={icon} zIndexOffset={20}>
      <Popup>I am a car-{id}!</Popup>
    </RotatedMarker>
  );
};

export default React.memo(Car);
