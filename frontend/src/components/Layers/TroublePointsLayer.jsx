import React from "react";
import { connect } from "react-redux";

import TroublePoint from "../Markers/TroublePoint";

const TroublePointsLayer = props => {
  const { troublePoints = [] } = props;
  const troublePointMarkers = troublePoints.map((troublePoint, ind) => <TroublePoint key={ind} troublePoint={troublePoint} />);

  return <div>{troublePointMarkers}</div>;
};

const mapStateToProps = (state /* , ownProps */) => {
  const { message } = state;
  return {
    troublePoints: message.troublePoints,
  };
};

export default connect(mapStateToProps)(TroublePointsLayer);
