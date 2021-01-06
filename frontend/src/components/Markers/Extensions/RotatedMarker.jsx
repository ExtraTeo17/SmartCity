import React from "react";
import { withLeaflet, Marker } from "react-leaflet";
import "leaflet-rotatedmarker";

/**
 * @category Markers
 * @subcategory Other
 */

/**
 * Custom marker component used for rotation control. <br/>
 * resources: {@link https://stackoverflow.com/questions/53778772/react-leaflet-rotatedmarkers-typeerror-super-expression-must-either-be-null-or}
 * @function
 * @memberof module:Extensions
 * @param {*} props
 */
const RotatedMarker = props => {
  const setupMarker = marker => {
    if (marker) {
      if (props.rotationAngle) marker.leafletElement.setRotationAngle(props.rotationAngle);
      if (props.rotationOrigin) marker.leafletElement.setRotationOrigin(props.rotationOrigin);
      if (props.customRef) props.customRef(marker);
    }
  };

  // eslint-disable-next-line react/jsx-props-no-spreading
  return <Marker ref={setupMarker} {...props} />;
};

RotatedMarker.defaultProps = {
  rotationOrigin: "center",
};

export default withLeaflet(RotatedMarker);
