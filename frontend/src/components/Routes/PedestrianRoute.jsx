import React from "react";
import { Polyline } from "react-leaflet";

import { generateRandomColor } from "../../utils/helpers";
import { COLORS_NUMBER, DEFAULT_WEIGHT } from "./constants";

const pathColors = new Map();
for (let pathsIter = 0; pathsIter < COLORS_NUMBER * 2; ++pathsIter) {
  pathColors.set(pathsIter, generateRandomColor());
}

const PedestrianRoute = props => {
  const { pedestrianId, route, isTestPedestrianRoute } = props;

  const color = pathColors.get(pedestrianId % pathColors.size);

  return (
    <>
      {route && (
        <Polyline
          weight={isTestPedestrianRoute ? DEFAULT_WEIGHT + 1 : DEFAULT_WEIGHT}
          color={color}
          positions={route}
          bubblingMouseEvents={false}
          interactive={false}
        />
      )}
    </>
  );
};

export default PedestrianRoute;
