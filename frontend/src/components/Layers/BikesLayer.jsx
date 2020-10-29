import React from "react";
import { connect } from "react-redux";

import Bike from "../Markers/Bike";
import BikeRoute from "../Routes/BikeRoute";

const BikesLayer = props => {
  const { bikes = [] } = props;

  const bikeMarkers = bikes.map(bike => <Bike key={`${bike.id}bike`} bike={bike} />);
  const bikeRoutes = bikes.map(bike => (
    <BikeRoute key={`${bike.id}routePed`} bikeId={bike.id} route={bike.route} isTestBikeRoute={bike.isTestBike} />
  ));

  return (
    <>
      {bikeMarkers}
      {bikeRoutes}
    </>
  );
};

const mapStateToProps = (state /* , ownProps */) => ({
  bikes: state.bike.bikes,
});

export default connect(mapStateToProps)(React.memo(BikesLayer));
