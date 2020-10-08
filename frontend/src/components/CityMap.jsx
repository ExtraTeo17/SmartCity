import React, { useState, useEffect } from "react";
import { Map, Marker, Popup, TileLayer, Circle, CircleMarker } from "react-leaflet";
import "../styles/CityMap.css";
import { connect } from "react-redux";
import Car from "./Markers/Car";
import Light from "./Markers/Light";

const DEFAULT_ZOOM = 15;
const MAX_ZOOM = 20;
const MAX_NATIVE_ZOOM = 19;

const CityMap = props => {
  const [zoom, setZoom] = useState(DEFAULT_ZOOM);
  const { lat, lng, rad } = props.center;
  const { lights, cars } = props;

  // Similar to componentDidMount and componentDidUpdate:
  useEffect(() => {});

  const lightMarkers = lights.map((light, ind) => <Light key={ind} light={light} />);

  const carMarkers = cars.map((car, ind) => <Car key={ind} car={car}></Car>);

  return (
    <Map
      center={{ lat, lng }}
      zoom={zoom}
      preferCanvas={true}
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
          <Popup>Zone center</Popup>
        </Marker>
        {carMarkers}
        {lightMarkers}
      </Circle>
    </Map>
  );
};

const mapStateToProps = (state /* , ownProps */) => {
  const { interaction, message } = state;
  return {
    center: interaction.center,
    lights: message.lights,
    cars: message.cars.slice(),
  };
};

export default connect(mapStateToProps)(CityMap);
