/* eslint-disable react/prop-types */
import React, { useState } from "react";
import { Map, Marker, Popup, TileLayer, Circle } from "react-leaflet";
import "../styles/CityMap.css";
import { connect } from "react-redux";

const CityMap = props => {
  const [zoom, setZoom] = useState(15);
  const { lat, lng, rad } = props.center;

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
      />
      <Circle center={{ lat, lng }} radius={rad}>
        <Marker position={{ lat, lng }}>
          <Popup>
            A pretty CSS3 popup. <br /> Easily customizable.
          </Popup>
        </Marker>
      </Circle>
    </Map>
  );
};

const mapStateToProps = (state /* , ownProps */) => {
  return {
    center: state.center,
  };
};

export default connect(mapStateToProps)(CityMap);
