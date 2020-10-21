import React from "react";
import { connect } from "react-redux";
import MarkerClusterGroup from "react-leaflet-markercluster";
import { bigPedestrianIcon } from "../../styles/icons";

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

  const iconCreate = () => bigPedestrianIcon;

  return (
    <>
      <MarkerClusterGroup showCoverageOnHover={false} iconCreateFunction={iconCreate} animateAddingMarkers maxClusterRadius={10}>
        {pedestrianMarkers}
      </MarkerClusterGroup>
      {pedestrianRoutes}
    </>
  );
};

const mapStateToProps = (state /* , ownProps */) => {
  const { message } = state;
  return {
    pedestrians: message.pedestrians,
  };
};

export default connect(mapStateToProps)(React.memo(PedestriansLayer));
