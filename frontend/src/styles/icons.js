import L from "leaflet";

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
  iconAnchor: [16, 16],
  popupAnchor: [8, 8],
  iconSize: [32, 32],
});

export const testCarIcon = new L.Icon({
  iconUrl: "/images/test_car.png",
  iconRetinaUrl: "/images/test_car.png",
  iconAnchor: [16, 16],
  popupAnchor: [8, 8],
  iconSize: [32, 32],
});
