import React from "react";
import { Polyline } from "react-leaflet";

import RouteChangePoint from "../Markers/RouteChangePoint";
import { generateRandomColor } from "../../utils/helpers";
import { COLORS_NUMBER, DEFAULT_WEIGHT } from "./constants";

const pathColors = new Map();
for (let pathsIter = 0; pathsIter < COLORS_NUMBER; ++pathsIter) {
  pathColors.set(pathsIter, generateRandomColor());
}

const CarRoute = props => {
  const { carId, route, isTestCarRoute, routeChangePoint } = props;

  const color = pathColors.get(carId % pathColors.size);

  return (
    <div>
      {route && (
        <Polyline
          weight={isTestCarRoute ? DEFAULT_WEIGHT + 1 : DEFAULT_WEIGHT}
          color={color}
          positions={route}
          bubblingMouseEvents={false}
          interactive={false}
        />
      )}
      {routeChangePoint && <RouteChangePoint point={routeChangePoint} />}
    </div>
  );
};

export default CarRoute;
