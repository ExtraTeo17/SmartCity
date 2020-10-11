import React, { useEffect, useState } from "react";
import { Circle, Map, Marker, Polyline, Popup, TileLayer } from "react-leaflet";
import { connect } from "react-redux";
import { dispatch } from "../redux/store";
import { centerUpdated } from "../redux/actions";
import "../styles/CityMap.css";
import Car from "./Markers/Car";
import Light from "./Markers/Light";
import { generateRandomColor } from "../utils/helpers";

const DEFAULT_ZOOM = 15;
const MAX_ZOOM = 20;
const MAX_NATIVE_ZOOM = 19;
const pathColors = [];

const CityMap = props => {
  const [zoom, setZoom] = useState(DEFAULT_ZOOM);
  const { lat, lng, rad } = props.center;
  const { lights, cars } = props;

  // Similar to componentDidMount and componentDidUpdate:
  useEffect(() => {
    if (pathColors.length < cars.length) {
      const colorsToAdd = cars.length - pathColors.length;
      for (let i = 0; i < colorsToAdd; ++i) {
        pathColors.push(generateRandomColor());
      }
    }
  });

  function setCenter(latlng) {
    const { lat, lng } = latlng;
    dispatch(centerUpdated({ lat, lng, rad }));
  }

  const lightMarkers = lights.map((light, ind) => <Light key={ind} location={light} />);
  const carMarkers = cars.map((car, ind) => <Car key={ind} car={car}></Car>);
  const carRoutes = cars.map((car, ind) => (
    <Polyline key={ind} weight={car.isTestCar ? 4 : 3} color={pathColors[ind]} positions={car.route} />
  ));

  return (
    <Map
      center={{ lat, lng }}
      zoom={zoom}
      preferCanvas={true}
      onzoomanim={e => {
        setZoom(e.zoom);
      }}
      oncontextmenu={e => setCenter(e.latlng)}
    >
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
        {carMarkers}
        {carRoutes}
        {lightMarkers}
      </Circle>
    </Map>
  );
};

const mapStateToProps = (state /* , ownProps */) => {
  const { interaction, message } = state;
  return {
    center: interaction.center,
    lights: message.lightLocations,
    cars: message.cars,
  };
};

export default connect(mapStateToProps)(CityMap);
