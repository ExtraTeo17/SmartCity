import React from "react";
import { Marker, Popup } from "react-leaflet";
import { STATIC_Z_INDEX } from "../../constants/markers";
import { troublePointIcon, accidentIcon } from "../../styles/icons";

/**
 * TroublePoint marker.
 * @category Markers
 * @module TroublePoint
 */

/**
 * @typedef {Object} Props
 * @property {TroublePoint} troublePoint
 */

/**
 * @typedef {Object} TroublePoint
 * @property {number} id
 * @property {boolean} isConstruction - if is construction site (accident otherwise)
 * @property {Position} location
 */

/**
 * @typedef {Object} Position - Represents position on map
 * @property {number} lat - Latitude in degrees
 * @property {number} lng - Longitude in degrees
 */

/**
 * TroublePoint component
 * @function
 * @param {Props} props
 */
const TroublePoint = props => {
  const {
    troublePoint: { isConstruction, location },
  } = props;
  const icon = isConstruction ? troublePointIcon : accidentIcon;

  return (
    <Marker position={location} opacity={0.95} icon={icon} zIndexOffset={STATIC_Z_INDEX}>
      <Popup>I am {isConstruction ? "a construction site" : "an accident"}!</Popup>
    </Marker>
  );
};

export default React.memo(TroublePoint);
