import React from "react";
import { FeatureGroup } from "react-leaflet";
import { connect } from "react-redux";
import busesSelector from "../../redux/selectors/busesSelector";

import Bus from "../Markers/Bus";
import BusRoute from "../Routes/BusRoute";

/**
 * Buses layer - contains all bus markers and routes
 * @category Layers
 * @module BusesLayer
 */

/**
 * @typedef {Object} Props
 * @property {Object[]} buses
 */

/**
 * @param {Props} props
 */
const BusesLayer = props => {
  const { buses = [] } = props;

  const busMarkers = buses.map(bus => <Bus key={`bus${bus.id}`} bus={bus} />);
  const busRoutes = buses.map(bus => <BusRoute key={`busRoute${bus.id}`} busId={bus.id} route={bus.route} />);

  return (
    <FeatureGroup>
      {busMarkers}
      {busRoutes}
    </FeatureGroup>
  );
};

const mapStateToProps = (state /* , ownProps */) => {
  return {
    buses: busesSelector(state),
  };
};

export default connect(mapStateToProps)(React.memo(BusesLayer));
