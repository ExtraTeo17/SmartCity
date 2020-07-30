import React, { useState } from "react";
import ApiManager from "../web/ApiManager";

const Menu = props => {
  const [latitude, setLatitude] = useState(52.217154);
  const [longitude, setLongtitude] = useState(21.01681);
  const [radius, setRadius] = useState(200);

  return (
    <div className="row justify-content-center">
      <form>
        <div className="form-group">
          <label htmlFor="lat">Latitude</label>
          <input
            type="number"
            defaultValue={latitude}
            className="form-control"
            id="lat"
            placeholder="Enter latitude"
            onChange={e => setLatitude(parseFloat(e.target.value))}
          />
        </div>
        <div className="form-group">
          <label htmlFor="long">Longitude</label>
          <input
            type="number"
            defaultValue={longitude}
            className="form-control"
            id="long"
            placeholder="Enter longitude"
            onChange={e => setLongtitude(parseFloat(e.target.value))}
          />
        </div>
        <div className="form-group">
          <label htmlFor="rad">Radius</label>
          <input
            type="number"
            defaultValue={radius}
            className="form-control"
            id="rad"
            placeholder="Enter radius"
            onChange={e => setRadius(parseFloat(e.target.value))}
          />
        </div>
        <button
          className="btn btn-primary"
          type="button"
          onClick={() => ApiManager.setZone({ latitude, longitude, radius })}
        >
          Set zone
        </button>
      </form>
    </div>
  );
};

export default Menu;
