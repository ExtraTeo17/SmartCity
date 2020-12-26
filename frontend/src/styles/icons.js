import L from "leaflet";

/**
 * Contains configuration of all marker icons
 * @category Main
 * @module icons
 */

export const greenLightIcon = new L.Icon({
  iconUrl: "/images/light_green.png",
  iconRetinaUrl: "/images/light_green.png",
  iconAnchor: [10, 17],
  popupAnchor: [5, 9],
  iconSize: [20, 34],
});

export const redLightIcon = new L.Icon({
  iconUrl: "/images/light_red.png",
  iconRetinaUrl: "/images/light_red.png",
  iconAnchor: [10, 17],
  popupAnchor: [5, 9],
  iconSize: [20, 34],
});

export const carIcon = new L.Icon({
  iconUrl: "/images/car.png",
  iconRetinaUrl: "/images/car.png",
  popupAnchor: [3, 7],
  iconAnchor: [7, 15],
  iconSize: [15, 30],
});

export const testCarIcon = new L.Icon({
  iconUrl: "/images/test_car.png",
  iconRetinaUrl: "/images/test_car.png",
  popupAnchor: [3, 7],
  iconAnchor: [7, 15],
  iconSize: [15, 30],
});

export const stationIcon = new L.Icon({
  iconUrl: "/images/bus_stop.png",
  iconRetinaUrl: "/images/bus_stop.png",
  iconAnchor: [16, 16],
  popupAnchor: [8, 8],
  iconSize: [32, 32],
});

export const busLowIcon = new L.Icon({
  iconUrl: "/images/bus_low.png",
  iconRetinaUrl: "/images/bus_low.png",
  popupAnchor: [4, 10],
  iconAnchor: [9, 21],
  iconSize: [18, 42],
});

export const busMidIcon = new L.Icon({
  iconUrl: "/images/bus_mid.png",
  iconRetinaUrl: "/images/bus_mid.png",
  popupAnchor: [4, 10],
  iconAnchor: [9, 21],
  iconSize: [18, 42],
});

export const busHighIcon = new L.Icon({
  iconUrl: "/images/bus_high.png",
  iconRetinaUrl: "/images/bus_high.png",
  popupAnchor: [4, 10],
  iconAnchor: [9, 21],
  iconSize: [18, 42],
});

export const pedestrianIcon = new L.Icon({
  iconUrl: "/images/pedestrian.png",
  iconRetinaUrl: "/images/pedestrian.png",
  popupAnchor: [4, 2],
  iconAnchor: [8, 5],
  iconSize: [16, 11],
});

export const testPedestrianIcon = new L.Icon({
  iconUrl: "/images/test_pedestrian.png",
  iconRetinaUrl: "/images/test_pedestrian.png",
  popupAnchor: [4, 3],
  iconAnchor: [8, 7],
  iconSize: [16, 15],
});

export const troublePointIcon = new L.Icon({
  iconUrl: "/images/trouble_point.png",
  iconRetinaUrl: "/images/trouble_point.png",
  iconAnchor: [14, 14],
  popupAnchor: [7, 7],
  iconSize: [28, 28],
});

export const accidentIcon = new L.Icon({
  iconUrl: "/images/accident.png",
  iconRetinaUrl: "/images/accident.png",
  popupAnchor: [8, 8],
  iconAnchor: [16, 16],
  iconSize: [32, 32],
});

export const goldMarkerIcon = new L.Icon({
  iconUrl: "/images/marker-icon-gold.png",
  iconRetinaUrl: "/images/marker-icon-gold.png",
  iconSize: [25, 41],
  popupAnchor: [1, -34],
  iconAnchor: [12, 41],
});

export const bikeIcon = new L.Icon({
  iconUrl: "/images/bike.png",
  iconRetinaUrl: "/images/bike.png",
  popupAnchor: [7, 10],
  iconAnchor: [14, 20],
  iconSize: [28, 40],
});

export const testBikeIcon = new L.Icon({
  iconUrl: "/images/test_bike.png",
  iconRetinaUrl: "/images/test_bike.png",
  popupAnchor: [7, 10],
  iconAnchor: [14, 20],
  iconSize: [28, 40],
});
