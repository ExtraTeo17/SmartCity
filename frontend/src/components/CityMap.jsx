import React, { useState, useEffect } from "react";
import { Map, Marker, Popup, TileLayer, Circle, CircleMarker } from "react-leaflet";
import "../styles/CityMap.css";
import { connect } from "react-redux";
import { greenLightIcon } from "../styles/icons";

const DEFAULT_ZOOM = 15;
const MAX_ZOOM = 20;
const MAX_NATIVE_ZOOM = 19;

const CityMap = props => {
  const [zoom, setZoom] = useState(DEFAULT_ZOOM);
  const { lat, lng, rad } = props.center;
  const { lights } = props;

  // Similar to componentDidMount and componentDidUpdate:
  useEffect(() => {});

  const lightMarkers = lights.map((light, ind) => (
    <Marker key={ind} position={light} opacity={0.95} icon={greenLightIcon}>
      <Popup>I am a light!</Popup>
    </Marker>
  ));

  return (
    <Map
      center={{ lat, lng }}
      zoom={zoom}
      onzoomanim={e => {
        setZoom(e.zoom);
      }}
    >
      <TileLayer
        attribution='&amp;copy <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        maxZoom={MAX_ZOOM}
        maxNativeZoom={MAX_NATIVE_ZOOM}
      />
      <Circle center={{ lat, lng }} radius={rad}>
        <Marker position={{ lat, lng }} interactive={true}>
          <Popup>
            A pretty CSS3 popup. <br /> Easily customizable.
          </Popup>
        </Marker>
        {lightMarkers}
      </Circle>
    </Map>
  );
};

const mapStateToProps = (state /* , ownProps */) => {
  const { interaction, message } = state;
  return {
    center: interaction.center,
    lights: message.lightLocations.slice(),
  };
};

export default connect(mapStateToProps)(CityMap);
