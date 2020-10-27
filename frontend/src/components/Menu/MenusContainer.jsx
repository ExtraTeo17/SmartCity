import React from "react";
import { connect } from "react-redux";
import Tabs from "react-bootstrap/Tabs";
import Tab from "react-bootstrap/Tab";
import Menu from "./Main/Menu";
import StrategyMenu from "./StrategyMenu";
import ResultsMenu from "./ResultsMenu";
import "../../styles/MenusContainer.css";

const MenusContainer = ({ timeResults }) => {
  return (
    <Tabs defaultActiveKey="main" variant="tabs" id="tabbed-menu">
      <Tab eventKey="main" title="Main">
        <Menu />
      </Tab>
      <Tab eventKey="strategy" title="Strategy">
        <StrategyMenu />
      </Tab>
      <Tab eventKey="contact" title="Setups" disabled>
        <div className="form-border">Predefined setups (ex. Test Bus Zone) will be here.</div>
      </Tab>
      <Tab eventKey="results" title="Results" disabled={timeResults.length === 0}>
        <ResultsMenu />
      </Tab>
    </Tabs>
  );
};

const mapStateToProps = (state /* , ownProps */) => {
  const { timeResults } = state.message;
  return {
    timeResults,
  };
};

export default connect(mapStateToProps)(React.memo(MenusContainer));
