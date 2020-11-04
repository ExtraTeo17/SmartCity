import React from "react";
import { FeatureGroup } from "react-leaflet";
import { connect } from "react-redux";
import pedestrianSelector from "../../redux/selectors/pedestriansSelector";

import Pedestrian from "../Markers/Pedestrian";
import PedestrianRoute from "../Routes/PedestrianRoute";

const PedestriansLayer = props => {
  const { pedestrians = [] } = props;

  const pedestrianMarkers = pedestrians.map(pedestrian => <Pedestrian key={`${pedestrian.id}ped`} pedestrian={pedestrian} />);
  const pedestrianRoutes = pedestrians.map(pedestrian => (
    <PedestrianRoute
      key={`${pedestrian.id}routePed`}
      pedestrianId={pedestrian.id}
      route={pedestrian.route}
      isTestPedestrianRoute={pedestrian.isTestPedestrian}
    />
  ));

  return (
    <FeatureGroup>
      {pedestrianMarkers}
      {pedestrianRoutes}
    </FeatureGroup>
  );
};

const mapStateToProps = (state /* , ownProps */) => ({
  pedestrians: pedestrianSelector(state),
});

export default connect(mapStateToProps)(React.memo(PedestriansLayer));
