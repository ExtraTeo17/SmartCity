import React, { useState, useEffect, useRef } from "react";
import { connect } from "react-redux";
import Tabs from "react-bootstrap/Tabs";
import Tab from "react-bootstrap/Tab";
import Menu from "./Main/Menu";
import StrategyMenu from "./StrategyMenu";
import ResultsMenu from "./ResultsMenu";
import "../../styles/MenusContainer.css";
import ScenariosMenu from "./ScenariosMenu";
import LimitsMenu from "./LimitsMenu";

const animationEffectsCount = 4;

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
  const [strategyClassName, setStrategyClassName] = useState("");

  useEffect(() => {
    if (wasPrepared) {
      setKey("main");
    }
  }, [wasPrepared]);

  const firstUpdate = useRef(0);

  function createEffect(arg = false, setFuncArray = []) {
    return () => {
      if (firstUpdate.current < animationEffectsCount) {
        firstUpdate.current += 1;
        return;
      }

      if (arg) {
        setFuncArray.forEach(setFunc => setFunc("tab-animate"));
      } else {
        setFuncArray.forEach(setFunc => setFunc(""));
      }
    };
  }

  useEffect(createEffect(generatePedestrians, [setLimitsClassName, setStrategyClassName]), [generatePedestrians]);
  useEffect(createEffect(generateCars, [setLimitsClassName, setStrategyClassName]), [generateCars]);
  useEffect(createEffect(generateBikes, [setLimitsClassName]), [generateBikes]);
  useEffect(createEffect(generateTroublePoints, [setLimitsClassName, setStrategyClassName]), [generateTroublePoints]);

  const onSelect = key => {
    if (key === "limits") {
      setLimitsClassName("");
    } else if (key === "strategy") {
      setStrategyClassName("");
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
      <Tab eventKey="strategy" title="Strategy" tabClassName={strategyClassName}>
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
    prepareSimulationData: { generatePedestrians },
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
