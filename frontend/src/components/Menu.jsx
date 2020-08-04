/* eslint-disable react/prop-types */
import React, { useState } from "react";
import ApiManager from "../web/ApiManager";
import { connect } from "react-redux";
import { centerUpdated } from "../redux/actions";

const Menu = props => {
  const { lat, lng, rad } = props.center;
  const dispatch = props.dispatch;

  /**
   * @param {number} val
   */
  const setLat = val => {
    if (!isNaN(val)) {
      let center = { ...props.center, lat: val };
      dispatch(centerUpdated(center));
    }
  };

  /**
   * @param {number} val
   */
  const setLng = val => {
    if (!isNaN(val)) {
      let center = { ...props.center, lng: val };
      dispatch(centerUpdated(center));
    }
  };

  /**
   * @param {number} val
   */
  const setRad = val => {
    if (!isNaN(val)) {
      let center = { ...props.center, rad: val };
      dispatch(centerUpdated(center));
    }
  };

  return (
    <div className="row justify-content-center">
      <form>
        <div className="form-group">
          <label htmlFor="lat">Latitude</label>
          <input
            type="number"
            defaultValue={lat}
            className="form-control"
            id="lat"
            step="0.0001"
            min="-90"
            max="90"
            placeholder="Enter latitude"
            onChange={e => setLat(parseFloat(e.target.value))}
          />
        </div>
        <div className="form-group">
          <label htmlFor="long">Longitude</label>
          <input
            type="number"
            defaultValue={lng}
            className="form-control"
            id="long"
            step="0.0001"
            min="-180"
            max="180"
            placeholder="Enter longitude"
            onChange={e => setLng(parseFloat(e.target.value))}
          />
        </div>
        <div className="form-group">
          <label htmlFor="rad">Radius</label>
          <input
            type="number"
            defaultValue={rad}
            className="form-control"
            id="rad"
            step="10"
            max="10000"
            placeholder="Enter radius"
            onChange={e => setRad(parseFloat(e.target.value))}
          />
        </div>
        <button className="btn btn-primary" type="button" onClick={() => ApiManager.setZone({ lat, lng, rad })}>
          Set zone
        </button>
      </form>
    </div>
  );
};

const mapStateToProps = (state /* , ownProps */) => {
  return {
    center: state.interaction.center,
  };
};

export default connect(mapStateToProps)(Menu);
