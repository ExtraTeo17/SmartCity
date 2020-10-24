import React from "react";
import { connect } from "react-redux";

import TroublePoint from "../Markers/TroublePoint";

const TroublePointsLayer = props => {
  const { troublePoints = [] } = props;
  const troublePointMarkers = troublePoints.map(tp => <TroublePoint key={`tp${tp.id}`} troublePoint={tp} />);

  return <>{troublePointMarkers}</>;
};

const mapStateToProps = (state /* , ownProps */) => {
  const { message } = state;
  return {
    troublePoints: message.troublePoints,
  };
};

export default connect(mapStateToProps)(TroublePointsLayer);
