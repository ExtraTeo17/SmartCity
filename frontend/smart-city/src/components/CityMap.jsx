import React, { useState } from "react";
import { Map, Marker, Popup, TileLayer } from "react-leaflet";

const CityMap = props => {
  const [position, setPosition] = useState([52.217154, 21.005575]);
  const [zoom, setZoom] = useState(15);

  return (
    <Map center={position} zoom={zoom}>
      <TileLayer
        attribution='&amp;copy <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />
      <Marker position={position}>
        <Popup>
          A pretty CSS3 popup. <br /> Easily customizable.
        </Popup>
      </Marker>
    </Map>
  );
};

export default CityMap;
