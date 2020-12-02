import React from "react";
import { FeatureGroup } from "react-leaflet";
import { connect } from "react-redux";

import TroublePoint from "../Markers/TroublePoint";

const TroublePointsLayer = props => {
  const { troublePoints = [] } = props;
  const troublePointMarkers = troublePoints.map(tp => <TroublePoint key={`tp${tp.id}`} troublePoint={tp} />);

  return <FeatureGroup>{troublePointMarkers}</FeatureGroup>;
};

const mapStateToProps = (state /* , ownProps */) => {
  const { message } = state;
  return {
    troublePoints: message.troublePoints,
  };
};

export default connect(mapStateToProps)(TroublePointsLayer);
