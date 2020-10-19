import React from "react";
import { connect } from "react-redux";

import Car from "../Markers/Car";
import CarRoute from "../Routes/CarRoute";

const CarsLayer = props => {
  const { cars = [] } = props;

  const carMarkers = cars.map(car => <Car key={car.id} car={car} />);
  const carRoutes = cars.map((car, ind) => (
    <CarRoute
      key={cars.length + ind}
      carId={car.id}
      route={car.route}
      isTestCarRoute={car.isTestCar}
      routeChangePoint={car.routeChangePoint}
    />
  ));

  return (
    <div>
      {carMarkers}
      {carRoutes}
    </div>
  );
};

const mapStateToProps = (state /* , ownProps */) => {
  const { message } = state;
  return {
    cars: message.cars.filter(c => c.isDeleted === undefined),
  };
};

export default connect(mapStateToProps)(CarsLayer);
