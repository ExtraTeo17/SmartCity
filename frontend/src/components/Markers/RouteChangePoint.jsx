import React from "react";
import { Popup, CircleMarker } from "react-leaflet";
import { STATIC_Z_INDEX } from "../../constants/markers";

/**
 * RouteChangePoint marker - used with car routes.
 * @category Markers
 * @module RouteChangePoint
 */

/**
 * @typedef {Object} Props
 * @property {Position} point
 */

/**
 * @typedef {Object} Position - Represents position on map
 * @property {number} lat - Latitude in degrees
 * @property {number} lng - Longitude in degrees
 */

/**
 * RouteChangePoint component
 * @function
 * @param {Props} props
 */
const RouteChangePoint = props => {
  const { point } = props;

  return point ? (
    <CircleMarker
      center={point}
      radius={8}
      color="yellow"
      opacity={0.75}
      fillColor="yellow"
      fillOpacity={0.85}
      zIndexOffset={STATIC_Z_INDEX + 2}
    >
      <Popup>Route changed here!</Popup>
    </CircleMarker>
  ) : null;
};

export default React.memo(RouteChangePoint);
