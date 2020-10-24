import React from "react";
import { connect } from "react-redux";

import Light from "../Markers/Light";

const LightsLayer = props => {
  const { lights = [] } = props;
  // eslint-disable-next-line react/no-array-index-key
  const lightMarkers = lights.map(light => <Light key={`$light${light.id}`} light={light} />);

  return <>{lightMarkers}</>;
};

const mapStateToProps = (state /* , ownProps */) => {
  const { message } = state;
  return {
    lights: message.lights,
  };
};

export default connect(mapStateToProps)(LightsLayer);
