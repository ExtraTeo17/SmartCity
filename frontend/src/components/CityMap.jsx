import React, { useEffect, useState } from "react";
import { Circle, Map as LeafletMap, Marker, Polyline, Popup, TileLayer } from "react-leaflet";
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

const DEFAULT_WEIGHT = 3;

const pathColors = new Map();

const CityMap = props => {
  const [zoom, setZoom] = useState(DEFAULT_ZOOM);
  const { lat, lng, rad } = props.center;
  const { lights = [], cars = [] } = props;

  // Similar to componentDidMount and componentDidUpdate:
  useEffect(() => {
    if (pathColors.size < cars.length) {
      for (let carsIter = pathColors.size; carsIter < cars.length; ++carsIter) {
        const colorKey = cars[carsIter].id;
        pathColors.set(colorKey, generateRandomColor());
      }
    }
  });

  function setCenter(latlng) {
    const { lat, lng } = latlng;
    dispatch(centerUpdated({ lat, lng, rad }));
  }

  const lightMarkers = lights.map((light, ind) => <Light key={ind} location={light} />);
  const carMarkers = cars.map(car => (car.isDeleted ? null : <Car key={car.id} car={car} />));
  const carRoutes = cars.map((car, ind) =>
    car.isDeleted ? null : (
      <Polyline
        key={ind}
        weight={car.isTestCar ? DEFAULT_WEIGHT + 1 : DEFAULT_WEIGHT}
        color={pathColors.get(car.id)}
        positions={car.route}
      />
    )
  );

  return (
    <LeafletMap
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
    </LeafletMap>
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
