import React from "react";
import { connect } from "react-redux";
import { FeatureGroup } from "react-leaflet";

import Bike from "../Markers/Bike";
import BikeRoute from "../Routes/BikeRoute";

/**
 * Bikes layer - contains all bike markers and routes
 * @category Layers
 * @module BikesLayer
 */

/**
 * @typedef {Object} Props
 * @property {Object[]} bikes
 */

/**
 * @param {Props} props
 */
const BikesLayer = props => {
  const { bikes = [] } = props;

  const bikeMarkers = bikes.map(bike => <Bike key={`${bike.id}bike`} bike={bike} />);
  const bikeRoutes = bikes.map(bike => (
    <BikeRoute key={`${bike.id}routePed`} bikeId={bike.id} route={bike.route} isTestBikeRoute={bike.isTestBike} />
  ));

  return (
    <FeatureGroup>
      {bikeMarkers}
      {bikeRoutes}
    </FeatureGroup>
  );
};

const mapStateToProps = (state /* , ownProps */) => ({
  bikes: state.bike.bikes,
});

export default connect(mapStateToProps)(React.memo(BikesLayer));
