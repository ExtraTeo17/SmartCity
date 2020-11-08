import React, { useEffect, useReducer } from "react";
import { Popup } from "react-leaflet";
import { BIKE_ROTATION_THRESHOLD } from "../../constants/thresholds";
import { bikeIcon, testBikeIcon } from "../../styles/icons";
import { angleFromCoordinates } from "../../utils/helpers";
import { getRotationReducer } from "./Extensions/reducers";
import RotatedMarker from "./Extensions/RotatedMarker";

const Bike = props => {
  const {
    bike: { id, route, location, isTestBike },
  } = props;

  const defaultAngle = route ? angleFromCoordinates(route[0], route[1]) : 0;
  const [state, dispatch] = useReducer(getRotationReducer(BIKE_ROTATION_THRESHOLD), { loc: location, angle: defaultAngle });

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
