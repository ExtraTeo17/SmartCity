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

    const hash = getLocationHash(b.location);
    if (positionsSet.has(hash) === false) {
      positionsSet.add(hash);
      busSet.add(b.id);
      result.push(b);
      return;
    }

    const routeStart = b.route[0];
    const routeEnd = b.route[b.route.length - 1];
    // TODO: Compute hash here
    const boundaries = [routeStart, routeEnd];
    if (routesSet.has(boundaries) === false) {
      routesSet.add(boundaries);
      busSet.add(b.id);
      result.push(b);
    }
  });

  return result;
});
