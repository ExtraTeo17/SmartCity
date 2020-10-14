import React from "react";
import { connect } from "react-redux";

import Station from "../Markers/Station";

const StationLayer = props => {
  const { stations = [] } = props;
  const stationMarkers = stations.map((station, ind) => <Station key={ind} station={station} />);

  return <div>{stationMarkers}</div>;
};

const mapStateToProps = (state /* , ownProps */) => {
  const { message } = state;
  return {
    stations: message.stations,
  };
};

export default connect(mapStateToProps)(StationLayer);
