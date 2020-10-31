import React, { useState, useEffect } from "react";
import { connect } from "react-redux";
import Tabs from "react-bootstrap/Tabs";
import Tab from "react-bootstrap/Tab";
import Menu from "./Main/Menu";
import StrategyMenu from "./StrategyMenu";
import ResultsMenu from "./ResultsMenu";
import "../../styles/MenusContainer.css";
import SetupsMenu from "./SetupsMenu";
import LimitsMenu from "./LimitsMenu";

const MenusContainer = ({ timeResults, wasPrepared, wasStarted }) => {
  const [key, setKey] = useState("strategy");

  useEffect(() => {
    if (wasPrepared) {
      setKey("main");
    }
  }, [wasPrepared]);

  return (
    <Tabs activeKey={key} onSelect={k => setKey(k)} variant="tabs" id="tabbed-menu">
      <Tab eventKey="main" title="Main">
        <Menu />
      </Tab>
      <Tab eventKey="limits" title="Limits">
        <LimitsMenu />
      </Tab>
      <Tab eventKey="strategy" title="Strategy">
        <StrategyMenu />
      </Tab>
      {!wasStarted && (
        <Tab eventKey="contact" title="Setups">
          <SetupsMenu />
        </Tab>
      )}
      {wasStarted && (
        <Tab eventKey="results" title="Results" disabled={timeResults.length === 0}>
          <ResultsMenu />
        </Tab>
      )}
    </Tabs>
  );
};

const mapStateToProps = (state /* , ownProps */) => {
  const { timeResults, wasPrepared, wasStarted } = state.message;
  return {
    timeResults,
    wasPrepared,
    wasStarted,
  };
};

export default connect(mapStateToProps)(React.memo(MenusContainer));
