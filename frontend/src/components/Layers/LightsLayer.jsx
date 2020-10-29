import React from "react";
import { connect } from "react-redux";

import Light from "../Markers/Light";

const LightsLayer = props => {
  const { lights = [] } = props;
  const lightMarkers = lights.map((light, ind) => <Light key={`$light${light.id + ind}`} light={light} />);

  return <>{lightMarkers}</>;
};

const mapStateToProps = (state /* , ownProps */) => {
  const { message } = state;
  return {
    lights: message.lights,
  };
};

export default connect(mapStateToProps)(LightsLayer);
