import React, { useEffect } from "react";
import { Polyline } from "react-leaflet";
import { connect } from "react-redux";

import Car from "../Markers/Car";
import { generateRandomColor } from "../../utils/helpers";

const DEFAULT_WEIGHT = 3;

const pathColors = new Map();
const CarsLayer = props => {
  const { cars = [] } = props;

  useEffect(() => {
    if (pathColors.size < cars.length) {
      for (let carsIter = pathColors.size; carsIter < cars.length; ++carsIter) {
        pathColors.set(carsIter, generateRandomColor());
      }
    }
  });

  const carMarkers = cars.map(car => (car.isDeleted ? null : <Car key={car.id} car={car} />));
  const carRoutes = cars.map((car, ind) =>
    car.isDeleted || !car.route ? null : (
      <Polyline
        key={ind}
        weight={car.isTestCar ? DEFAULT_WEIGHT + 1 : DEFAULT_WEIGHT}
        color={pathColors.get(car.id % pathColors.size)}
        positions={car.route}
      />
    )
  );

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
    cars: message.cars,
  };
};

export default connect(mapStateToProps)(CarsLayer);
