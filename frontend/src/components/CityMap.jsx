import React, { useState } from "react";
import { Circle, Map as LeafletMap, Marker, Popup, TileLayer } from "react-leaflet";
import { connect } from "react-redux";
import { notify } from "react-notify-toast";
import { dispatch } from "../redux/store";
import { centerUpdated } from "../redux/core/actions";

import "../styles/CityMap.css";
import LightsLayer from "./Layers/LightsLayer";
import StationsLayer from "./Layers/StationsLayer";
import TroublePointsLayer from "./Layers/TroublePointsLayer";
import CarsLayer from "./Layers/CarsLayer";
import { NOTIFY_SHOW_MS } from "../utils/constants";
import BusesLayer from "./Layers/BusesLayer";
import PedestriansLayer from "./Layers/PedestriansLayer";

const DEFAULT_ZOOM = 15;
const MAX_ZOOM = 20;
const MAX_NATIVE_ZOOM = 19;

const CityMap = ({ center: { lat, lng, rad }, wasStarted }) => {
  const [zoom, setZoom] = useState(DEFAULT_ZOOM);

  function setCenter(latlng) {
    const { lat, lng } = latlng;
    dispatch(centerUpdated({ lat, lng, rad }));
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
    <LeafletMap center={{ lat, lng }} zoom={zoom} preferCanvas onzoomanim={onZoom} oncontextmenu={rightClickOnMap}>
      <TileLayer
        attribution='&amp;copy <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        maxZoom={MAX_ZOOM}
        maxNativeZoom={MAX_NATIVE_ZOOM}
      />

      <Circle center={{ lat, lng }} radius={rad}>
        <Marker position={{ lat, lng }}>
          <Popup>Zone center</Popup>
        </Marker>
      </Circle>
      <CarsLayer />
      <LightsLayer />
      <StationsLayer />
      <BusesLayer />
      <TroublePointsLayer />
      <PedestriansLayer />
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
