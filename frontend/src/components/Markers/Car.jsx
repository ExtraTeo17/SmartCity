import React, { useEffect, useReducer } from "react";
import { Popup } from "react-leaflet";
import { CAR_MOVING_Z_INDEX, CAR_ROTATION_THRESHOLD } from "../../constants/markers";
import { carIcon, testCarIcon } from "../../styles/icons";
import { angleFromCoordinates } from "../../utils/helpers";
import { getRotationReducer } from "./Extensions/reducers";
import RotatedMarker from "./Extensions/RotatedMarker";

/**
 * Car marker.
 * @category Markers
 * @module Car
 */

/**
 * @typedef {Object} Props
 * @property {Car} car
 */

/**
 * @typedef {Object} Car
 * @property {number} id
 * @property {Position[]} route
 * @property {Position} location
 * @property {boolean} isTestCar
 */

/**
 * @typedef {Object} Position - Represents position on map
 * @property {number} lat - Latitude in degrees
 * @property {number} lng - Longitude in degrees
 */

/**
 * Car component
 * @function
 * @param {Props} props
 */
const Car = props => {
  const {
    car: { id, route, location, isTestCar },
  } = props;

  const defaultAngle = route ? angleFromCoordinates(route[0], route[1]) : 0;
  const [state, dispatch] = useReducer(getRotationReducer(CAR_ROTATION_THRESHOLD), { loc: location, angle: defaultAngle });

  useEffect(() => {
    // @ts-ignore
    dispatch({ payload: location });
  }, [location]);

  const icon = isTestCar ? testCarIcon : carIcon;
  const zOffset = isTestCar ? CAR_MOVING_Z_INDEX + 1 : CAR_MOVING_Z_INDEX;

  return (
    <RotatedMarker rotationAngle={state.angle} rotationOrigin="center" position={state.loc} icon={icon} zIndexOffset={zOffset}>
      <Popup>I am a car-{id}!</Popup>
    </RotatedMarker>
  );
};

export default React.memo(Car);
