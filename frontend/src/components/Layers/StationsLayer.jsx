import React from "react";
import { FeatureGroup } from "react-leaflet";
import { connect } from "react-redux";

import Station from "../Markers/Station";

/**
 * Stations layer - contains all stations markers.
 * @category Layers
 * @module StationLayer
 */

/**
 * @typedef {Object} Props
 * @property {Object[]} stations
 */

/**
 * @param {Props} props
 */
const StationLayer = props => {
  const { stations = [] } = props;
  const stationMarkers = stations.map(station => <Station key={`station${station.id}`} station={station} />);

  return <FeatureGroup>{stationMarkers}</FeatureGroup>;
};

const mapStateToProps = (state /* , ownProps */) => {
  const { message } = state;
  return {
    stations: message.stations,
  };
};

export default connect(mapStateToProps)(StationLayer);
