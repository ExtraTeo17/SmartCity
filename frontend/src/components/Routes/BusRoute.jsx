import React from "react";
import { Polyline } from "react-leaflet";

import { generateRandomColor } from "../../utils/helpers";
import { COLORS_NUMBER, DEFAULT_WEIGHT } from "./constants";

const pathColors = new Map();
for (let pathsIter = 0; pathsIter < COLORS_NUMBER; ++pathsIter) {
  pathColors.set(pathsIter, generateRandomColor());
}

const BusRoute = props => {
  const { busId, route } = props;

  const color = pathColors.get(busId % pathColors.size);

  return (
    <div>
      {route && (
        <Polyline weight={DEFAULT_WEIGHT} color={color} positions={route} bubblingMouseEvents={false} interactive={false} />
      )}
    </div>
  );
};

export default React.memo(BusRoute);
