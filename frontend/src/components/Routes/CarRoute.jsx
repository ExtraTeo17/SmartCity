import React from "react";
import { Polyline } from "react-leaflet";

import RouteChangePoint from "../Markers/RouteChangePoint";
import { generateRandomColor } from "../../utils/helpers";
import { COLORS_NUMBER, DEFAULT_WEIGHT } from "./constants";

/**
 * Car route
 * @category Routes
 * @module CarRoute
 */

const pathColors = new Map();
for (let pathsIter = 0; pathsIter < COLORS_NUMBER; ++pathsIter) {
  pathColors.set(pathsIter, generateRandomColor());
}

/**
 * @typedef {Object} Props
 * @property {number} carId
 * @property {Position[]} route
 * @property {boolean} isTestCarRoute
 * @property {Position} routeChangePoint
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
const CarRoute = props => {
  const { carId, route, isTestCarRoute, routeChangePoint } = props;

  const color = pathColors.get(carId % pathColors.size);

  return (
    <>
      {route && (
        <Polyline
          weight={isTestCarRoute ? DEFAULT_WEIGHT + 1 : DEFAULT_WEIGHT}
          color={color}
          positions={route}
          bubblingMouseEvents={false}
          interactive={false}
        />
      )}
      {routeChangePoint && <RouteChangePoint point={routeChangePoint} />}
    </>
  );
};

export default React.memo(CarRoute);
