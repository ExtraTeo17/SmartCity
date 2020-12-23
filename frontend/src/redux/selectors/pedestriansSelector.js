import { createSelector } from "reselect";

const getPedestrians = state => state.pedestrian.pedestrians;

/**
 * For filtering arrays stored in redux state
 * @category Redux
 * @module selectors
 */

/**
 * Filters pedestrian to display based on `hidden` attribute.
 * Used to hide specific pedestrians (e.g. in bus).
 * @func pedestriansSelector
 * @returns {Object[]} - filtered pedestrians
 */
export default createSelector([getPedestrians], pedestrians => pedestrians.filter(p => p.hidden !== true));
