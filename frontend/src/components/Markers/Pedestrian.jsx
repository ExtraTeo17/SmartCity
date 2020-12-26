import React, { useEffect, useReducer } from "react";
import { Popup } from "react-leaflet";
import { PEDESTRIAN_ROTATION_THRESHOLD, TEST_PEDESTRIAN_Z_INDEX, PEDESTRIAN_Z_INDEX } from "../../constants/markers";
import { pedestrianIcon, testPedestrianIcon } from "../../styles/icons";
import { angleFromCoordinates } from "../../utils/helpers";
import { getRotationReducer } from "./Extensions/reducers";
import RotatedMarker from "./Extensions/RotatedMarker";

/**
 * Pedestrian marker.
 * @category Markers
 * @module Pedestrian
 */

/**
 * @typedef {Object} Props
 * @property {Pedestrian} pedestrian
 */

/**
 * @typedef {Object} Pedestrian
 * @property {number} id
 * @property {Position[]} route
 * @property {Position} location
 * @property {boolean} isTestPedestrian
 */

/**
 * @typedef {Object} Position - Represents position on map
 * @property {number} lat - Latitude in degrees
 * @property {number} lng - Longitude in degrees
 */

/**
 * Pedestrian component
 * @function
 * @param {Props} props
 */
const Pedestrian = props => {
  const {
    pedestrian: { id, route, location, isTestPedestrian },
  } = props;

  const defaultAngle = route ? angleFromCoordinates(route[0], route[1]) : 0;
  const [state, dispatch] = useReducer(getRotationReducer(PEDESTRIAN_ROTATION_THRESHOLD), { loc: location, angle: defaultAngle });

  useEffect(() => {
    // @ts-ignore
    dispatch({ payload: location });
  }, [location]);

  const icon = isTestPedestrian ? testPedestrianIcon : pedestrianIcon;
  const zIndex = isTestPedestrian ? TEST_PEDESTRIAN_Z_INDEX : PEDESTRIAN_Z_INDEX;

  return (
    <RotatedMarker rotationAngle={state.angle} rotationOrigin="center" position={state.loc} icon={icon} zIndexOffset={zIndex}>
      <Popup>I am a pedestrian-{id}</Popup>
    </RotatedMarker>
  );
};

export default React.memo(Pedestrian);
