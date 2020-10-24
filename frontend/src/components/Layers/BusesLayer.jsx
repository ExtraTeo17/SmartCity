import React from "react";
import { connect } from "react-redux";

import Bus from "../Markers/Bus";
import BusRoute from "../Routes/BusRoute";

const BusesLayer = props => {
  const { buses = [] } = props;

  const busMarkers = buses.map(bus => <Bus key={`bus${bus.id}`} bus={bus} />);
  const busRoutes = buses.map(bus => <BusRoute key={`busRoute${bus.id}`} busId={bus.id} route={bus.route} />);

  return (
    <>
      {busMarkers}
      {busRoutes}
    </>
  );
};

const mapStateToProps = (state /* , ownProps */) => {
  const { buses } = state.bus;
  return {
    buses,
  };
};

export default connect(mapStateToProps)(React.memo(BusesLayer));
