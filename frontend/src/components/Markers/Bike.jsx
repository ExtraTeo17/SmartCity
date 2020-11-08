import React, { useEffect, useReducer } from "react";
import { Popup } from "react-leaflet";
import { BIKE_ROTATION_THRESHOLD } from "../../constants/thresholds";
import { bikeIcon, testBikeIcon } from "../../styles/icons";
import { angleFromCoordinates } from "../../utils/helpers";
import RotatedMarker from "./Extensions/RotatedMarker";

function rotationReducer(state = { loc: 0, angle: 0 }, action) {
  const newLocation = action.payload;
  if (state.loc !== newLocation) {
    const newAngle = angleFromCoordinates(state.loc, newLocation);
    if (Math.abs(newAngle - state.angle) > BIKE_ROTATION_THRESHOLD) {
      return { loc: newLocation, angle: newAngle };
    }

    return { ...state, loc: newLocation };
  }

  return state;
}

const Bike = props => {
  const {
    bike: { id, route, location, isTestBike },
  } = props;

  const defaultAngle = route ? angleFromCoordinates(route[0], route[1]) : 0;
  const [state, dispatch] = useReducer(rotationReducer, { loc: location, angle: defaultAngle });

  useEffect(() => {
    dispatch({ payload: location });
  }, [location]);

  const icon = isTestBike ? testBikeIcon : bikeIcon;

  return (
    <RotatedMarker rotationAngle={state.angle} rotationOrigin="center" position={state.loc} icon={icon} zIndexOffset={20}>
      <Popup>I am a bike-{id}!</Popup>
    </RotatedMarker>
  );
};

export default React.memo(Bike);
