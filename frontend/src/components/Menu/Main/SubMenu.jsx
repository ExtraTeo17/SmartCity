/* eslint-disable no-restricted-globals */
import { connect } from "react-redux";
import React from "react";
import { dispatch } from "../../../redux/store";
import { startSimulationDataUpdated } from "../../../redux/core/actions";
import "../../../styles/Menu.css";

const SubMenu = props => {
  const { wasStarted, useFixedRoutes } = props;

  function dispatchUpdate(data) {
    dispatch(startSimulationDataUpdated(data));
  }
  function evSetUseFixedRoutes(e) {
    dispatchUpdate({ useFixedRoutes: e.target.checked });
  }

  return (
    <div>
      <div className="form-check user-select-none">
        <input
          type="checkbox"
          className="form-check-input"
          id="useFixedRoutes"
          checked={useFixedRoutes}
          disabled={wasStarted}
          onChange={evSetUseFixedRoutes}
        />
        <label htmlFor="useFixedRoutes" className="form-check-label">
          Use fixed routes
        </label>
      </div>
    </div>
  );
};

const mapStateToProps = (state /* ownProps */) => {
  const { wasStarted } = state.message;
  const {
    startSimulationData: { useFixedRoutes },
  } = state.interaction;
  return {
    wasStarted,
    useFixedRoutes,
  };
};

export default connect(mapStateToProps)(React.memo(SubMenu));
