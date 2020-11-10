import React, { useState, useEffect } from "react";
import { connect } from "react-redux";
import Tabs from "react-bootstrap/Tabs";
import Tab from "react-bootstrap/Tab";
import Menu from "./Main/Menu";
import StrategyMenu from "./StrategyMenu";
import ResultsMenu from "./ResultsMenu";
import "../../styles/MenusContainer.css";
import ScenariosMenu from "./ScenariosMenu";
import LimitsMenu from "./LimitsMenu";

const MenusContainer = ({
  timeResults,
  wasPrepared,
  wasStarted,
  generatePedestrians,
  generateCars,
  generateBikes,
  generateTroublePoints,
}) => {
  const [key, setKey] = useState("main");
  const [limitsClassName, setLimitsClassName] = useState("");

  useEffect(() => {
    if (wasPrepared) {
      setKey("main");
    }
  }, [wasPrepared]);

  function createEffect(arg) {
    return () => {
      if (arg) {
        setLimitsClassName("tab-animate");
      } else {
        setLimitsClassName("");
      }
    };
  }

  useEffect(createEffect(generatePedestrians), [generatePedestrians]);
  useEffect(createEffect(generateCars), [generateCars]);
  useEffect(createEffect(generateBikes), [generateBikes]);
  useEffect(createEffect(generateTroublePoints), [generateTroublePoints]);

  const onSelect = key => {
    if (key === "limits") {
      setLimitsClassName("");
    }
    setKey(key);
  };

  return (
    <Tabs activeKey={key} onSelect={onSelect} variant="tabs" id="tabbed-menu">
      <Tab eventKey="main" title="Main">
        <Menu />
      </Tab>
      <Tab eventKey="limits" title="Limits" tabClassName={limitsClassName}>
        <LimitsMenu />
      </Tab>
      <Tab eventKey="strategy" title="Strategy">
        <StrategyMenu />
      </Tab>
      {!wasStarted && (
        <Tab eventKey="scenarios" title="Scenarios">
          <ScenariosMenu />
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
  const {
    startSimulationData: { generateCars, generateBikes, generateTroublePoints },
    generatePedestrians,
  } = state.interaction;

  return {
    timeResults,
    wasPrepared,
    wasStarted,
    generatePedestrians,
    generateCars,
    generateBikes,
    generateTroublePoints,
  };
};

export default connect(mapStateToProps)(React.memo(MenusContainer));
