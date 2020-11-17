import React from "react";
import { FeatureGroup } from "react-leaflet";
import { connect } from "react-redux";

import TroublePoint from "../Markers/TroublePoint";

const TroublePointsLayer = props => {
  const { troublePoints = [], useFixedTroublePoints = false } = props;
  const troublePointMarkers = troublePoints.map(tp => (
    <TroublePoint key={`tp${tp.id}`} useFixed={useFixedTroublePoints} troublePoint={tp} />
  ));

  return <FeatureGroup>{troublePointMarkers}</FeatureGroup>;
};

const mapStateToProps = (state /* , ownProps */) => {
  const { message, interaction } = state;
  return {
    troublePoints: message.troublePoints,
    useFixedTroublePoints: interaction.startSimulationData.useFixedTroublePoints,
  };
};

export default connect(mapStateToProps)(TroublePointsLayer);
