import React from "react";
import { Polyline } from "react-leaflet";

import { generateRandomColor } from "../../utils/helpers";
import { COLORS_NUMBER, DEFAULT_WEIGHT } from "./constants";

/**
 * Bus route
 * @category Routes
 * @module BusRoute
 */

const pathColors = new Map();
for (let pathsIter = 0; pathsIter < COLORS_NUMBER; ++pathsIter) {
  pathColors.set(pathsIter, generateRandomColor());
}

/**
 * @typedef {Object} Props
 * @property {number} busId
 * @property {Position[]} route
 */

/**
 * @typedef {Object} Position - Represents position on map
 * @property {number} lat - Latitude in degrees
 * @property {number} lng - Longitude in degrees
 */

/**
 * Bus route component
 * @function
 * @param {Props} props
 */
const BusRoute = props => {
  const { busId, route } = props;

  const color = pathColors.get(busId % pathColors.size);

  return (
    <>
      {route && (
        <Polyline weight={DEFAULT_WEIGHT} color={color} positions={route} bubblingMouseEvents={false} interactive={false} />
      )}
    </>
  );
};

export default React.memo(BusRoute);
