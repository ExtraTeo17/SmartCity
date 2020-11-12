import { createSelector } from "reselect";
import { getLocationHash } from "../../utils/helpers";

const positionsSet = new Set();
const routesSet = new Set();
const busSet = new Set();
const getBuses = state => state.bus.buses;

export default createSelector([getBuses], buses => {
  const result = new Array(buses.length);
  buses.forEach(b => {
    if (busSet.has(b.id)) {
      result.push(b);
      return;
    }

    const locHash = getLocationHash(b.location);
    if (positionsSet.has(locHash) === false) {
      positionsSet.add(locHash);
      busSet.add(b.id);
      result.push(b);
      return;
    }

    if (b.route) {
      const routeStart = b.route[0];
      const routeEnd = b.route[b.route.length - 1];
      const routeHash = 100000 * getLocationHash(routeStart) + getLocationHash(routeEnd);
      if (routesSet.has(routeHash) === false) {
        routesSet.add(routeHash);
        busSet.add(b.id);
        result.push(b);
      }
    }
  });

  return result;
});
