import React from "react";
import { Polyline } from "react-leaflet";

import { generateRandomColor } from "../../utils/helpers";
import { COLORS_NUMBER, DEFAULT_WEIGHT } from "./constants";

/**
 * Pedestrian route
 * @category Routes
 * @module PedestrianRoute
 */

const pathColors = new Map();
for (let pathsIter = 0; pathsIter < COLORS_NUMBER * 2; ++pathsIter) {
  pathColors.set(pathsIter, generateRandomColor());
}

/**
 * @typedef {Object} Props
 * @property {number} pedestrianId
 * @property {Position[]} route
 * @property {boolean} isTestPedestrianRoute
 */

/**
 * @typedef {Object} Position - Represents position on map
 * @property {number} lat - Latitude in degrees
 * @property {number} lng - Longitude in degrees
 */

/**
 * Car route component
 * @function
 * @param {Props} props
 */
const PedestrianRoute = props => {
  const { pedestrianId, route, isTestPedestrianRoute } = props;

  const color = pathColors.get(pedestrianId % pathColors.size);

  return (
    <>
      {route && (
        <Polyline
          weight={isTestPedestrianRoute ? DEFAULT_WEIGHT + 1 : DEFAULT_WEIGHT}
          color={color}
          positions={route}
          bubblingMouseEvents={false}
          interactive={false}
        />
      )}
    </>
  );
};

export default React.memo(PedestrianRoute);
