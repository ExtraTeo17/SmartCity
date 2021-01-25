/**
 * Defines global constants
 * @category Constants
 * @module global
 */

/**
 * Time before attempting to reconnect to server again in case of failure
 * @constant
 */
export const RECONNECT_INTERVAL_SEC = 5;

/**
 * Default time in ms before notification hide
 * @constant
 * @default 3000
 */
export const NOTIFY_SHOW_MS = 3000;

/**
 * Color of info-type notification
 * @constant
 */
export const NOTIFY_INFO_COLOR = { background: "#6495ED", text: "#FFFFFF" };

export const IS_DEBUG = false;

export const HERE_MAP_API_KEY = "k9SbyKjlOTgu9oLPjfgab956ZcFWucHc3RPSJsX_Jqw";

/**
 * Key for local storage
 * @constant
 */
export const storageKey = "startSimulationData";
