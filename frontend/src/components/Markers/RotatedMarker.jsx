import React from "react";
import { withLeaflet, Marker } from "react-leaflet";
import "leaflet-rotatedmarker";

// https://stackoverflow.com/questions/53778772/react-leaflet-rotatedmarkers-typeerror-super-expression-must-either-be-null-or
const RotatedMarker = props => {
  const setupMarker = marker => {
    if (marker) {
      if (props.rotationAngle) marker.leafletElement.setRotationAngle(props.rotationAngle);
      marker.leafletElement.setRotationOrigin(props.rotationOrigin);
    }
  };

  return <Marker ref={setupMarker} {...props} />;
};

RotatedMarker.defaultProps = {
  rotationOrigin: "center",
};

export default withLeaflet(RotatedMarker);
