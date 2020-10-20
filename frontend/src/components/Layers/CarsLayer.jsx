import React from "react";
import { connect } from "react-redux";

import Car from "../Markers/Car";
import CarRoute from "../Routes/CarRoute";

const CarsLayer = props => {
  const { cars = [] } = props;

  const carMarkers = cars.map(car => <Car key={"car" + car.id} car={car} />);
  const carRoutes = cars.map(car => (
    <CarRoute
      key={"carRoute" + car.id}
      carId={car.id}
      route={car.route}
      isTestCarRoute={car.isTestCar}
      routeChangePoint={car.routeChangePoint}
    />
  ));

  return (
    <React.Fragment>
      {carMarkers}
      {carRoutes}
    </React.Fragment>
  );
};

const mapStateToProps = (state /* , ownProps */) => {
  const { message } = state;
  return {
    cars: message.cars,
  };
};

export default connect(mapStateToProps)(React.memo(CarsLayer));
