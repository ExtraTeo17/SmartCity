import React, { useState, useEffect } from "react";
import { Circle, LayersControl, Map as LeafletMap, Marker, Popup, FeatureGroup } from "react-leaflet";
import { connect } from "react-redux";
import { notify } from "react-notify-toast";
import { dispatch } from "../redux/store";
import { centerUpdated } from "../redux/core/actions";

import "../styles/CityMap.css";
import LightsLayer from "./Layers/LightsLayer";
import StationsLayer from "./Layers/StationsLayer";
import TroublePointsLayer from "./Layers/TroublePointsLayer";
import CarsLayer from "./Layers/CarsLayer";
import BikesLayer from "./Layers/BikesLayer";
import BusesLayer from "./Layers/BusesLayer";
import PedestriansLayer from "./Layers/PedestriansLayer";
import { NOTIFY_SHOW_MS } from "../constants/global";
import { D_ZOOM } from "../constants/defaults";
import { BaseLayers } from "./Layers/BaseLayers";

const { Overlay } = LayersControl;

const CityMap = ({ center, wasStarted }) => {
  const [zoom, setZoom] = useState(D_ZOOM);

  useEffect(() => {
    console.info("Rendered whole map!");
  });

  function setCenter(latlng) {
    const { lat, lng } = latlng;
    dispatch(centerUpdated({ lat, lng, rad: center.rad }));
  }

  function rightClickOnMap(e) {
    if (!wasStarted) {
      setCenter(e.latlng);
    } else {
      notify.show("You cannot move zone when simulation started!", "warning", NOTIFY_SHOW_MS);
    }
  }

  function onZoom(e) {
    setZoom(e.zoom);
  }

  return (
    <LeafletMap center={center} zoom={zoom} preferCanvas onzoomanim={onZoom} oncontextmenu={rightClickOnMap}>
      <LayersControl position="topright">
        {BaseLayers}

        <Overlay name="Zone" checked>
          <FeatureGroup>
            <Circle center={center} radius={center.rad} />
            <Marker position={center}>
              <Popup>Zone center</Popup>
            </Marker>
          </FeatureGroup>
        </Overlay>
        <Overlay name="Lights" checked>
          <LightsLayer />
        </Overlay>
        <Overlay name="Cars" checked>
          <CarsLayer />
        </Overlay>
        <Overlay name="Bikes" checked>
          <BikesLayer />
        </Overlay>
        <Overlay name="Pedestrians" checked>
          <PedestriansLayer />
        </Overlay>
        <Overlay name="Buses" checked>
          <BusesLayer />
        </Overlay>
        <Overlay name="Stations" checked>
          <StationsLayer />
        </Overlay>
        <Overlay name="Trouble points" checked>
          <TroublePointsLayer />
        </Overlay>
      </LayersControl>
    </LeafletMap>
  );
};

const mapStateToProps = (state /* , ownProps */) => {
  const { interaction, message } = state;
  return {
    center: interaction.center,
    wasStarted: message.wasStarted,
  };
};

export default connect(mapStateToProps)(React.memo(CityMap));
