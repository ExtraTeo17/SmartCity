import React, { useState } from "react";
import { Circle, LayersControl, Map as LeafletMap, Marker, Popup, TileLayer, FeatureGroup, LayerGroup } from "react-leaflet";
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
import { HERE_MAP_API_KEY, NOTIFY_SHOW_MS } from "../constants/global";
import { D_HERE_MAP_STYLE, D_ZOOM } from "../constants/defaults";
import { MAX_NATIVE_ZOOM, MAX_ZOOM } from "../constants/minMax";

const { BaseLayer, Overlay } = LayersControl;

const CityMap = ({ center, wasStarted }) => {
  const [zoom, setZoom] = useState(D_ZOOM);

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
        <BaseLayer checked name="Map - Main">
          <TileLayer
            attribution='&amp;copy <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            maxZoom={MAX_ZOOM}
            maxNativeZoom={MAX_NATIVE_ZOOM}
          />
        </BaseLayer>
        <BaseLayer name="Map - Black & White">
          <TileLayer
            attribution='&amp;copy <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
            url="https://tiles.wmflabs.org/bw-mapnik/{z}/{x}/{y}.png"
            maxZoom={MAX_ZOOM}
            maxNativeZoom={MAX_NATIVE_ZOOM}
          />
        </BaseLayer>
        <BaseLayer name="Map - Streets">
          <TileLayer
            attribution='&amp;copy; <a href="https://www.maptiler.com/copyright/">MapTiler</a> &amp;copy <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
            url="https://api.maptiler.com/maps/streets/{z}/{x}/{y}.png?key=JTQMBGnY9XYwZnvQilbM"
            maxZoom={MAX_ZOOM}
            maxNativeZoom={MAX_NATIVE_ZOOM}
          />
        </BaseLayer>
        <BaseLayer name="Map - Carto">
          <TileLayer
            attribution='&amp;copy; <a href="https://carto.com/about-carto/">Carto</a> &amp;copy <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
            url="https://{s}.basemaps.cartocdn.com/rastertiles/light_all/{z}/{x}/{y}.png"
            maxZoom={MAX_ZOOM}
            maxNativeZoom={MAX_NATIVE_ZOOM}
          />
        </BaseLayer>
        <BaseLayer name="Map - Here">
          <TileLayer
            attribution='&amp;copy; <a href="http://here.net/services/terms">HERE 2020</a> &amp;copy <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
            url={`https://2.aerial.maps.ls.hereapi.com/maptile/2.1/maptile/newest/${D_HERE_MAP_STYLE}/{z}/{x}/{y}/256/png8?apiKey=${HERE_MAP_API_KEY}`}
            maxZoom={MAX_ZOOM}
            maxNativeZoom={MAX_NATIVE_ZOOM}
          />
        </BaseLayer>

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
        <CarsLayer />
        <BikesLayer />
        <StationsLayer />
        <BusesLayer />
        <TroublePointsLayer />
        <PedestriansLayer />
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
