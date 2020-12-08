import { createSelector } from "reselect";
import { getLocationHash } from "../../utils/helpers";

const ROUTE_HASH_SHIFT = 100000;
const getBuses = state => state.bus.buses;

const getRouteHash = route => {
  const routeStart = route[0];
  const routeEnd = route[route.length - 1];
  return ROUTE_HASH_SHIFT * getLocationHash(routeStart) + getLocationHash(routeEnd);
};

const isRouteValid = route => route && route.length > 1;

/**
 * @category Redux
 */

/**
 * Filters buses to display based on position and route.
 * Used for performance reasons.
 * @func busesSelector
 * @memberof module:selectors
 * @returns {Object[]} - filtered buses
 */
export default createSelector([getBuses], buses => {
  const result = [];
  const positionsSet = new Set();
  const routesSet = new Set();
  buses.forEach(b => {
    if (b.startedMoving) {
      result.push(b);
      return;
    }

    const locHash = getLocationHash(b.location);
    if (!positionsSet.has(locHash)) {
      positionsSet.add(locHash);
      if (isRouteValid(b.route)) {
        routesSet.add(getRouteHash(b.route));
      }
      result.push(b);
      return;
    }

    const { route } = b;
    if (isRouteValid(route)) {
      const routeHash = getRouteHash(route);
      if (routesSet.has(routeHash) === false) {
        routesSet.add(routeHash);
        result.push(b);
      }
    }
  });

  return result;
});
