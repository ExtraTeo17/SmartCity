import React, { useEffect } from "react";
import { Polyline } from "react-leaflet";
import { connect } from "react-redux";

import Bus from "../Markers/Bus";
import { generateRandomColor } from "../../utils/helpers";
const DEFAULT_WEIGHT = 3;

const pathColors = new Map();
const BusesLayer = props => {
  const { buses = [] } = props;

  useEffect(() => {
    if (pathColors.size < buses.length) {
      for (let busesIter = pathColors.size; busesIter < buses.length; ++busesIter) {
        if (!pathColors.has(busesIter)) pathColors.set(busesIter, generateRandomColor());
      }
    }
  });

  const busMarkers = buses.map(bus => <Bus key={bus.id} bus={bus} />);
  const busRoutes = buses.map((bus, ind) => {
    const route = bus.route;
    if (!route) {
      return null;
    }

    const color = pathColors.get(bus.id % pathColors.size);

    return [
      <Polyline
        key={ind}
        weight={DEFAULT_WEIGHT}
        color={color}
        positions={route}
        bubblingMouseEvents={false}
        interactive={false}
      />,
    ];
  });

  return (
    <div>
      {busMarkers}
      {busRoutes}
    </div>
  );
};

const mapStateToProps = (state /* , ownProps */) => {
  const { message } = state;
  return {
    buses: message.buses.filter(c => c.isDeleted === undefined),
  };
};

export default connect(mapStateToProps)(BusesLayer);
