import React from "react";
import { batch, connect } from "react-redux";

import { centerUpdated, generatePedestriansUpdated } from "../../redux/core/actions";
import { dispatch } from "../../redux/store";
import ApiManager from "../../web/ApiManager";
import "../../styles/Menu.css";
import { IS_DEBUG } from "../../constants/global";

const prepareSimulation = data => {
  batch(() => {
    dispatch(centerUpdated(data));
    dispatch(generatePedestriansUpdated(data.generatePedestrians));
  });
  ApiManager.prepareSimulation(data);
};

const prepareCarZone = () => {
  prepareSimulation({ lat: 52.23682, lng: 21.01681, rad: 600, generatePedestrians: false });
};

const prepareBusZone = () => {
  prepareSimulation({ lat: 52.203342, lng: 20.861213, rad: 300, generatePedestrians: true });
};

const SetupsMenu = ({ wasStarted }) => {
  return (
    <div className="form-border">
      <button className="btn btn-primary btn-block" type="button" disabled={wasStarted} onClick={prepareCarZone}>
        Prepare car zone
      </button>
      <button className="btn btn-primary btn-block mt-5" type="button" disabled={wasStarted} onClick={prepareBusZone}>
        Prepare bus zone
      </button>
      {IS_DEBUG && (
        <button className="btn btn-primary btn-block mt-5" type="button" onClick={ApiManager.debug}>
          Debug
        </button>
      )}
    </div>
  );
};

const mapStateToProps = (state /* , ownProps */) => {
  const { wasStarted } = state.message;
  return {
    wasStarted,
  };
};

export default connect(mapStateToProps)(React.memo(SetupsMenu));
