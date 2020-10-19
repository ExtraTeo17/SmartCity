import React from "react";
import { connect } from "react-redux";

import Bus from "../Markers/Bus";
import BusRoute from "../Routes/BusRoute";

const BusesLayer = props => {
  const { buses = [] } = props;

  const busMarkers = buses.map(bus => <Bus key={bus.id} bus={bus} />);
  const busRoutes = buses.map((bus, ind) => <BusRoute key={buses.length + ind} busId={bus.id} route={bus.route} />);

  return (
    <div>
      {busMarkers}
      {busRoutes}
    </div>
  );
};

const mapStateToProps = (state /* , ownProps */) => {
  const { buses } = state.message;
  return {
    buses: buses,
  };
};

export default connect(mapStateToProps)(BusesLayer);
