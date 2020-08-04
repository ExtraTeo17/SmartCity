/* eslint-disable react/prop-types */
import React, { useState } from "react";
import { Map, Marker, Popup, TileLayer, Circle, CircleMarker } from "react-leaflet";
import "../styles/CityMap.css";
import { connect } from "react-redux";

const CityMap = props => {
  const [zoom, setZoom] = useState(15);
  const { lat, lng, rad } = props.center;
  const lights = props.lights;

  const lightMarkers = lights.forEach(light => (
    <CircleMarker center={light}>
      <Popup>I am a light!</Popup>
    </CircleMarker>
  ));

  return (
    <Map
      center={{ lat, lng }}
      zoom={zoom}
      onzoomanim={e => {
        setZoom(e.zoom);
      }}
      onclick={e => {
        console.log(lights);
        console.log(lights[0]);
      }}
    >
      <TileLayer
        attribution='&amp;copy <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />
      <Circle center={{ lat, lng }} radius={rad}>
        <Marker position={{ lat, lng }}>
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
