import React from "react";
import { Polyline } from "react-leaflet";

import { generateRandomColor } from "../../utils/helpers";
import { COLORS_NUMBER, DEFAULT_WEIGHT } from "./constants";

/**
 * Bike route
 * @category Routes
 * @module BikeRoute
 */

const pathColors = new Map();
for (let pathsIter = 0; pathsIter < COLORS_NUMBER; ++pathsIter) {
  pathColors.set(pathsIter, generateRandomColor(5, 0xc));
}

/**
 * @typedef {Object} Props
 * @property {number} bikeId
 * @property {Position[]} route
 * @property {boolean} isTestBikeRoute
 */

/**
 * @typedef {Object} Position - Represents position on map
 * @property {number} lat - Latitude in degrees
 * @property {number} lng - Longitude in degrees
 */

/**
 * Bike route component
 * @function
 * @param {Props} props
 */
const BikeRoute = props => {
  const { bikeId, route, isTestBikeRoute } = props;

  const color = pathColors.get(bikeId % pathColors.size);

  return (
    <>
      {route && (
        <Polyline
          weight={isTestBikeRoute ? DEFAULT_WEIGHT + 1 : DEFAULT_WEIGHT}
          color={color}
          positions={route}
          bubblingMouseEvents={false}
          interactive={false}
        />
      )}
    </>
  );
};

export default React.memo(BikeRoute);
