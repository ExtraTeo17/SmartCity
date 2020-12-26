import React, { useEffect, useReducer } from "react";
import { Popup } from "react-leaflet";
import { BIKE_MOVING_Z_INDEX, BIKE_ROTATION_THRESHOLD } from "../../constants/markers";
import { bikeIcon, testBikeIcon } from "../../styles/icons";
import { angleFromCoordinates } from "../../utils/helpers";
import { getRotationReducer } from "./Extensions/reducers";
import RotatedMarker from "./Extensions/RotatedMarker";

/**
 * Bike marker
 * @category Markers
 * @module Bike
 */

/**
 * @typedef {Object} Props
 * @property {Bike} bike
 */

/**
 * @typedef {Object} Bike
 * @property {number} id
 * @property {Position[]} route
 * @property {Position} location
 * @property {boolean} isTestBike
 */

/**
 * @typedef {Object} Position - Represents position on map
 * @property {number} lat - Latitude in degrees
 * @property {number} lng - Longitude in degrees
 */

/**
 * Bike component
 * @function
 * @param {Props} props
 */
const Bike = props => {
  const {
    bike: { id, route, location, isTestBike },
  } = props;

  const defaultAngle = route ? angleFromCoordinates(route[0], route[1]) : 0;
  const [state, dispatch] = useReducer(getRotationReducer(BIKE_ROTATION_THRESHOLD), { loc: location, angle: defaultAngle });

  useEffect(() => {
    // @ts-ignore
    dispatch({ payload: location });
  }, [location]);

  const icon = isTestBike ? testBikeIcon : bikeIcon;
  const zOffset = isTestBike ? BIKE_MOVING_Z_INDEX + 1 : BIKE_MOVING_Z_INDEX;

  return (
    <RotatedMarker rotationAngle={state.angle} rotationOrigin="center" position={state.loc} icon={icon} zIndexOffset={zOffset}>
      <Popup>I am a bike-{id}!</Popup>
    </RotatedMarker>
  );
};

export default React.memo(Bike);
