import React from "react";
import { connect } from "react-redux";

import Station from "../Markers/Station";

const StationLayer = props => {
  const { stations = [] } = props;
  const stationMarkers = stations.map(station => <Station key={`station${station.id}`} station={station} />);

  return <>{stationMarkers}</>;
};

const mapStateToProps = (state /* , ownProps */) => {
  const { message } = state;
  return {
    stations: message.stations,
  };
};

export default connect(mapStateToProps)(StationLayer);
