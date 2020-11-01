import React from "react";
import { LayersControl, TileLayer } from "react-leaflet";

import { HERE_MAP_API_KEY } from "../../constants/global";
import { D_HERE_MAP_STYLE } from "../../constants/defaults";
import { MAX_NATIVE_ZOOM, MAX_ZOOM } from "../../constants/minMax";

const { BaseLayer } = LayersControl;

export const BaseLayers = [
  <BaseLayer checked name="Map - Main">
    <TileLayer
      attribution='&amp;copy <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
      url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      maxZoom={MAX_ZOOM}
      maxNativeZoom={MAX_NATIVE_ZOOM}
    />
  </BaseLayer>,
  <BaseLayer name="Map - Black & White">
    <TileLayer
      attribution='&amp;copy <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
      url="https://tiles.wmflabs.org/bw-mapnik/{z}/{x}/{y}.png"
      maxZoom={MAX_ZOOM}
      maxNativeZoom={MAX_NATIVE_ZOOM}
    />
  </BaseLayer>,
  <BaseLayer name="Map - Streets">
    <TileLayer
      attribution='&amp;copy; <a href="https://www.maptiler.com/copyright/">MapTiler</a> &amp;copy <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
      url="https://api.maptiler.com/maps/streets/{z}/{x}/{y}.png?key=JTQMBGnY9XYwZnvQilbM"
      maxZoom={MAX_ZOOM}
      maxNativeZoom={MAX_NATIVE_ZOOM}
    />
  </BaseLayer>,
  <BaseLayer name="Map - Carto">
    <TileLayer
      attribution='&amp;copy; <a href="https://carto.com/about-carto/">Carto</a> &amp;copy <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
      url="https://{s}.basemaps.cartocdn.com/rastertiles/light_all/{z}/{x}/{y}.png"
      maxZoom={MAX_ZOOM}
      maxNativeZoom={MAX_NATIVE_ZOOM}
    />
  </BaseLayer>,
  <BaseLayer name="Map - Here">
    <TileLayer
      attribution='&amp;copy; <a href="http://here.net/services/terms">HERE 2020</a> &amp;copy <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
      url={`https://2.aerial.maps.ls.hereapi.com/maptile/2.1/maptile/newest/${D_HERE_MAP_STYLE}/{z}/{x}/{y}/256/png8?apiKey=${HERE_MAP_API_KEY}`}
      maxZoom={MAX_ZOOM}
      maxNativeZoom={MAX_NATIVE_ZOOM}
    />
  </BaseLayer>,
];
