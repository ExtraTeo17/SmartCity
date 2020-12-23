import React from "react";
import { Polyline } from "react-leaflet";

import { generateRandomColor } from "../../utils/helpers";
import { COLORS_NUMBER, DEFAULT_WEIGHT } from "./constants";

const pathColors = new Map();
for (let pathsIter = 0; pathsIter < COLORS_NUMBER; ++pathsIter) {
  pathColors.set(pathsIter, generateRandomColor(5, 0xc));
}

const BikeRoute = props => {
  const { bikeId, route, isTestBikeRoute } = props;

  const color = pathColors.get(bikeId % pathColors.size);

  return (
    <>
      {route && (
        <Polyline
          weight={isTestBikeRoute ? DEFAULT_WEIGHT + 1 : DEFAULT_WEIGHT}
          color={color}
          positions={route}
          bubblingMouseEvents={false}
          interactive={false}
        />
      )}
    </>
  );
};

export default React.memo(BikeRoute);
