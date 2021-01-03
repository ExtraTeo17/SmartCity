import React from "react";
import { Marker, Popup } from "react-leaflet";
import { STATIC_Z_INDEX } from "../../constants/markers";
import { stationIcon } from "../../styles/icons";

/**
 * Station marker.
 * @category Markers
 * @module Station
 */

/**
 * @typedef {Object} Props
 * @property {Station} station
 */

/**
 * @typedef {Object} Station
 * @property {number} id
 * @property {Position} location
 */

/**
 * @typedef {Object} Position - Represents position on map
 * @property {number} lat - Latitude in degrees
 * @property {number} lng - Longitude in degrees
 */

/**
 * Station component
 * @function
 * @param {Props} props
 */
const Station = props => {
  const {
    station: { id, location },
  } = props;

  return (
    <Marker position={location} opacity={0.95} icon={stationIcon} zIndexOffset={STATIC_Z_INDEX}>
      <Popup>I am a station-{id}!</Popup>
    </Marker>
  );
};

export default React.memo(Station);
