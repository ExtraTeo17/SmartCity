import { useEffect, useRef } from "react";
import { notify } from "react-notify-toast";
import { NOTIFY_SHOW_MS } from "../constants/global";

/**
 * Global helpers
 * @category Main
 * @module helpers
 */

/**
 * @typedef {Object} Position - Represents position on map
 * @property {number} lat - Latitude in degrees
 * @property {number} lng - Longitude in degrees
 */

/**
 * @typedef {Object} Event - Input event
 * @property {Object} target - Event target
 */

/**
 * {@link https://stackoverflow.com/a/1527820/6841224}
 * @function
 * @param {number} min
 * @param {number} max
 * @returns {number}
 */
export const getRandomInt = (min, max) => {
  const minC = Math.ceil(min);
  const maxF = Math.floor(max);
  return Math.floor(Math.random() * (maxF - minC + 1)) + minC;
};

/**
 * {@link https://stackoverflow.com/a/5365036/6841224}
 * @function
 * @param {number} minLight
 * @param {number} maxLight
 * @returns {String} - hex based color
 */
export const generateRandomColor = (minLight = 0, maxLight = 7) => {
  let result = 0;
  for (let i = 0; i < 6; ++i) {
    let colorDigit;
    if (i % 2 === 1) {
      colorDigit = getRandomInt(minLight, maxLight);
    } else {
      colorDigit = getRandomInt(0, 0xf);
    }
    result |= colorDigit << (4 * i);
  }

  return `#${result.toString(16)}`;
};

const precision = 1000; // lat and long precision, boost to 1000 if need be
const latOffset = 200; // Anything above 180 would do

/**
 * @function
 * @param {Position} loc
 * @returns {number} hash
 */
export const getLocationHash = loc => {
  return Number(loc.lat * precision * latOffset) + Number(loc.lng * precision);
};

/**
 * Add style for invalid input
 * @function
 * @param {Element} htmlELem input
 */
export const setInvalid = htmlELem => {
  htmlELem.classList.add("invalid-input");
};

/**
 * Remove style for invalid input
 * @function
 * @param {Element} htmlELem input
 */
export const setValid = htmlELem => {
  htmlELem.classList.remove("invalid-input");
};

function setIfValid(elem, parseFunc, min, max, setFunc) {
  const val = parseFunc(elem.value);
  // eslint-disable-next-line no-restricted-globals
  if (!isNaN(val) && val >= min && val <= max) {
    setValid(elem);
    setFunc(val);
  } else {
    setInvalid(elem);
  }
}

/**
 * Callback for setting value
 * @callback setInt
 * @param {number} val - integer value
 */

/**
 * @function
 * @param {Event} e
 * @param {number} min
 * @param {number} max
 * @param {setInt} setFunc
 */
export const setIfValidInt = (e, min, max, setFunc) => {
  setIfValid(e.target, parseInt, min, max, setFunc);
};

/**
 * Callback for setting value
 * @callback setFloat
 * @param {number} val - float value
 */

/**
 * @function
 * @param {Event} e
 * @param {number} min
 * @param {number} max
 * @param {setFloat} setFunc
 */
export const setIfValidFloat = (e, min, max, setFunc) => {
  setIfValid(e.target, parseFloat, min, max, setFunc);
};

/**
 * @function
 * @param {boolean} b
 * @returns {number} integer
 */
export const boolToInt = b => (b ? 1 : 0);

/**
 * @function
 * @param {any} value
 */
export const usePrevious = value => {
  const ref = useRef();
  useEffect(() => {
    ref.current = value;
  });
  return ref.current;
};

function degToRad(degrees) {
  return degrees * (Math.PI / 180);
}

function radToDeg(rad) {
  return rad * (180 / Math.PI);
}

/**
 * {@link https://stackoverflow.com/a/18738281/6841224}
 * @function
 * @param {Position} loc1
 * @param {Position} loc2
 * @returns {number} angle between two points
 */
export const angleFromCoordinates = (loc1, loc2) => {
  if (!loc1 || !loc2) {
    return 0;
  }

  const lat1Rad = degToRad(loc1.lat);
  const lat2Rad = degToRad(loc2.lat);

  const dLng = degToRad(loc2.lng - loc1.lng);

  const y = Math.sin(dLng) * Math.cos(lat2Rad);
  const x = Math.cos(lat1Rad) * Math.sin(lat2Rad) - Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(dLng);

  let heading = Math.atan2(y, x);
  heading = radToDeg(heading);
  heading = (heading + 360) % 360;
  // heading = 360 - heading; // count degrees counter-clockwise - remove to make clockwise

  return heading;
};

/**
 * Used for queued notifications
 * @function
 */
export const showQueued = notify.createShowQueue;

/**
 * Used notifying about unconnected backend
 * @function
 */
export const notifyWaitForConnection = () => {
  notify.show("Please wait for connection", "warning", NOTIFY_SHOW_MS / 2);
};

/**
 * Used notifying about unconnected backend
 * @param {Position} loc1
 * @param {Position} loc2
 * @function
 */
export const areLocationsEqual = (loc1, loc2) => {
  return loc1.lat === loc2.lat && loc1.lng === loc2.lng;
};

const EARTH_RADIUS = 6378137; // metres
/**
 * Calculation using harvesine formulae.
 * https://www.movable-type.co.uk/scripts/latlong.html
 * @param {Position} loc1
 * @param {Position} loc2
 * @returns Distance between 2 latlng points in meters
 */
export const calculateDistance = (loc1, loc2) => {
  const φ1 = degToRad(loc1.lat); // φ, λ in radians
  const φ2 = degToRad(loc2.lat);
  const Δφ = degToRad(loc2.lat - loc1.lat);
  const Δλ = degToRad(loc2.lng - loc1.lng);

  const a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) + Math.cos(φ1) * Math.cos(φ2) * Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

  return EARTH_RADIUS * c;
};
