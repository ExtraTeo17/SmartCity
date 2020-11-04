import React from "react";
import { FeatureGroup } from "react-leaflet";
import { connect } from "react-redux";

import Bus from "../Markers/Bus";
import BusRoute from "../Routes/BusRoute";

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
  const { buses } = state.bus;
  return {
    buses,
  };
};

export default connect(mapStateToProps)(React.memo(BusesLayer));
