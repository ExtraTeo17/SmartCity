import busesSelector from "../busesSelector";

beforeEach(() => {});

afterEach(() => {
  jest.clearAllMocks();
});

let state = {
  bus: {
    buses: [],
    ad: 0,
  },
};

const defLat = 52;
const defLng = 21.5;
const createBus = (id = 1, location = { lat: defLat, lng: defLng }, route = null) => {
  return { id, location, route };
};

const updateStateWithNewBuses = buses => {
  state = { bus: { ...state.bus, buses, ad: state.bus.ad + 1 } };
};

const createLoc = (lat, lng) => {
  return { lat, lng };
};

describe("Selector for buses without routes", () => {
  it("chooses only first bus with same location", () => {
    updateStateWithNewBuses([createBus(1), createBus(2), createBus(3)]);
    const result = busesSelector(state);
    expect(result).toHaveLength(1);
  });

  it("chooses new bus after location changed", () => {
    // Arrange
    updateStateWithNewBuses([createBus(1), createBus(2), createBus(3)]);
    const res1 = busesSelector(state);
    expect(res1).toHaveLength(1);

    // Act
    updateStateWithNewBuses([createBus(1, createLoc(33, 22)), createBus(2), createBus(3)]);
    const res2 = busesSelector(state);

    // Assert
    expect(res2).toHaveLength(2);
  });

  it("chooses new bus after location changed to same position as previous bus", () => {
    // Arrange
    updateStateWithNewBuses([createBus(1), createBus(2), createBus(3)]);
    const res1 = busesSelector(state);
    expect(res1).toHaveLength(1);

    updateStateWithNewBuses([createBus(1, createLoc(1, 2)), createBus(2), createBus(3)]);
    const res2 = busesSelector(state);
    expect(res2).toHaveLength(2);

    // Act
    updateStateWithNewBuses([createBus(1, createLoc(1, 3)), createBus(1, createLoc(1, 2)), createBus(3)]);
    const res3 = busesSelector(state);

    // Assert
    expect(res3).toHaveLength(3);
  });

  it("chooses many buses fast", () => {
    // Arrange
    const busesNum = 2500;
    let newBuses = new Array(busesNum);
    for (let i = 0; i < busesNum; ++i) {
      newBuses[i] = createBus(i);
    }
    updateStateWithNewBuses(newBuses);

    // Act & Assert
    const normalSelectMs = 20;
    const memoizeSelectMs = 1;

    // First select - init - normal
    let start = performance.now();
    let res1 = busesSelector(state);
    let end = performance.now();
    expect(res1).toHaveLength(1);
    expect(end - start).toBeLessThan(normalSelectMs);

    // Second select - no changes - memoize
    start = performance.now();
    res1 = busesSelector(state);
    end = performance.now();
    expect(res1).toHaveLength(1);
    expect(end - start).toBeLessThan(memoizeSelectMs);

    // Third select - all changed - normal
    newBuses = new Array(busesNum);
    for (let i = 0; i < busesNum; ++i) {
      newBuses[i] = createBus(i, { lat: defLat, lng: 1 + i / 1000 });
    }
    updateStateWithNewBuses(newBuses);

    start = performance.now();
    res1 = busesSelector(state);
    end = performance.now();
    expect(res1).toHaveLength(busesNum);
    expect(end - start).toBeLessThan(2 * normalSelectMs);

    // Fourth select - one moving - normal many times
    newBuses = new Array(busesNum);
    for (let i = 0; i < busesNum; ++i) {
      newBuses[i] = createBus(i, { lat: defLat, lng: defLng });
    }
    newBuses[0] = { ...newBuses[0], location: { lat: defLat, lng: 0.01 }, startedMoving: true };
    updateStateWithNewBuses(newBuses);

    let timeSum = 0;
    for (let i = 0; i < 100; ++i) {
      start = performance.now();
      res1 = busesSelector(state);
      timeSum += performance.now() - start;

      expect(res1).toHaveLength(2);

      newBuses = [...newBuses];
      newBuses[0] = { ...newBuses[0], location: { lat: defLat, lng: i + 0.3 } };
      updateStateWithNewBuses(newBuses);
    }
    expect(timeSum).toBeLessThan(normalSelectMs * 60);
  });
});

// With routes --------------------------------------------------------------------------------------------------------

const createBusWithRoute = (id = 1, route = null, location = { lat: defLat, lng: defLng }) => {
  return { id, location, route };
};

describe("Selector for buses with routes", () => {
  it("chooses only first bus with same route", () => {
    const route = [createLoc(1, 2), createLoc(1, 3), createLoc(1, 4)];
    updateStateWithNewBuses([createBusWithRoute(1, route), createBusWithRoute(2, route), createBusWithRoute(3, route)]);
    const result = busesSelector(state);
    expect(result).toHaveLength(1);
  });

  it("chooses many buses with different routes", () => {
    // Arrange
    const routeA = [createLoc(1, 2), createLoc(1, 3), createLoc(1, 4)];
    const routeB = [createLoc(1, 2), createLoc(1, 3), createLoc(2, 3)];
    let buses = [
      createBusWithRoute(1, routeA),
      createBusWithRoute(2, routeA),
      createBusWithRoute(3, routeB),
      createBusWithRoute(4, routeB),
    ];
    updateStateWithNewBuses(buses);

    // Act
    const res1 = busesSelector(state);

    // Assert - 2 different routes
    expect(res1).toHaveLength(2);

    // Arrange
    buses = buses.map(b => (b.id === 1 ? { ...b, location: createLoc(defLat, defLng + 1) } : b));
    updateStateWithNewBuses(buses);

    // Act
    const res2 = busesSelector(state);
    // Assert - 2 different routes + 2 different locations
    expect(res2).toHaveLength(3);
  });

  it("chooses new bus after location changed to same position as previous bus", () => {
    // Arrange
    const routeA = [createLoc(1, 2), createLoc(1, 3), createLoc(1, 4)];
    const routeB = [createLoc(1, 2), createLoc(1, 3), createLoc(2, 3)];
    const routeC = [createLoc(1, 1), createLoc(1, 3), createLoc(2, 3)];
    let buses = [
      createBusWithRoute(1, routeA),
      createBusWithRoute(2, routeA),
      createBusWithRoute(3, routeB),
      createBusWithRoute(4, routeC),
      createBusWithRoute(5, routeC),
    ];
    updateStateWithNewBuses(buses);

    const res1 = busesSelector(state);
    expect(res1).toHaveLength(3);

    buses = buses.map(b => (b.id === 4 ? { ...b, location: createLoc(defLat, defLng + 1), startedMoving: true } : b));
    updateStateWithNewBuses(buses);
    const res2 = busesSelector(state);
    // Assert - new location but previous is occupied by many other
    expect(res2).toHaveLength(4);

    buses = buses.map(b => (b.id === 5 ? { ...b, location: createLoc(defLat, defLng + 2), startedMoving: true } : b));
    updateStateWithNewBuses(buses);
    const res3 = busesSelector(state);
    // Assert - same routes but different locations now
    expect(res3).toHaveLength(4);

    buses = buses.map(b => (b.id === 3 ? { ...b, location: createLoc(defLat, defLng + 1), startedMoving: true } : b));
    updateStateWithNewBuses(buses);
    const res4 = busesSelector(state);
    // Assert - new location, but no other route of this type
    expect(res4).toHaveLength(4);

    buses = buses.map(b => (b.id === 4 ? { ...b, location: createLoc(defLat, defLng + 2) } : b));
    updateStateWithNewBuses(buses);
    const res5 = busesSelector(state);
    // Assert - new location, same as other with same route, but started moving
    expect(res5).toHaveLength(4);
  });

  it("chooses many buses fast", () => {
    // Arrange
    const busesNum = 2500;
    let newBuses = new Array(busesNum);
    const routeA = [createLoc(1, 2), createLoc(1, 3), createLoc(1, 4)];
    const routeB = [createLoc(1, 2), createLoc(1, 3), createLoc(2, 3)];
    const routeC = [createLoc(1, 1), createLoc(1, 3), createLoc(2, 3)];
    for (let i = 0; i < busesNum; ++i) {
      newBuses[i] = createBusWithRoute(i, routeA);
    }
    updateStateWithNewBuses(newBuses);

    // Act & Assert
    const normalSelectMs = 25;
    const memoizeSelectMs = 1;

    // First select - init - normal
    let start = performance.now();
    let res1 = busesSelector(state);
    let end = performance.now();
    expect(res1).toHaveLength(1);
    expect(end - start).toBeLessThan(normalSelectMs);

    // Second select - no changes - memoize
    start = performance.now();
    res1 = busesSelector(state);
    end = performance.now();
    expect(res1).toHaveLength(1);
    expect(end - start).toBeLessThan(memoizeSelectMs);

    // Third select - half changed route - normal
    newBuses = new Array(busesNum);
    for (let i = 0; i < busesNum; ++i) {
      newBuses[i] = createBusWithRoute(i, i & 1 ? routeB : routeA);
    }
    updateStateWithNewBuses(newBuses);

    start = performance.now();
    res1 = busesSelector(state);
    end = performance.now();
    expect(res1).toHaveLength(2);
    expect(end - start).toBeLessThan(1.6 * normalSelectMs);

    // Fourth select - one moving - normal many times
    newBuses = new Array(busesNum);
    for (let i = 0; i < busesNum; ++i) {
      newBuses[i] = createBusWithRoute(i, i & 1 ? routeB : routeA);
    }
    newBuses[0] = { ...newBuses[0], route: routeC, location: { lat: defLat, lng: 0.01 }, startedMoving: true };
    updateStateWithNewBuses(newBuses);

    let timeSum = 0;
    for (let i = 0; i < 100; ++i) {
      start = performance.now();
      res1 = busesSelector(state);
      timeSum += performance.now() - start;

      expect(res1).toHaveLength(3);

      newBuses = [...newBuses];

      newBuses[0] = { ...newBuses[0], location: { lat: defLat, lng: i + 0.3 } };
      updateStateWithNewBuses(newBuses);
    }
    expect(timeSum).toBeLessThan(normalSelectMs * 100);
  });
});
