import React from "react";
import { connect } from "react-redux";

import "../../styles/Menu.css";

const ResultsMenu = props => {
  const { timeResults } = props;

  const resultsList = timeResults.map(t => (
    <li key={`${t.type}${t.id}`} className="list-group-item list-group-item-dark">
      <b>{t.type}</b>: Travelled {t.distance}m in {t.time.minutes}m {t.time.seconds}s
    </li>
  ));

  return (
    <div className="form-border">
      <h3> Test objects results:</h3>
      <ul className="list-group">{resultsList}</ul>
    </div>
  );
};

const mapStateToProps = (state /* , ownProps */) => {
  const { timeResults } = state.message;
  return {
    timeResults,
  };
};

export default connect(mapStateToProps)(React.memo(ResultsMenu));
