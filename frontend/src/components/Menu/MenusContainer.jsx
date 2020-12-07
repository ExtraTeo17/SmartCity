import React, { useState, useEffect, useRef } from "react";
import { connect } from "react-redux";
import { notify } from "react-notify-toast";
import Tabs from "react-bootstrap/Tabs";
import Tab from "react-bootstrap/Tab";
import Menu from "./Main/Menu";
import StrategyMenu from "./StrategyMenu";
import ResultsMenu from "./ResultsMenu";
import ScenariosMenu from "./ScenariosMenu";
import LimitsMenu from "./LimitsMenu";
import { NOTIFY_INFO_COLOR, NOTIFY_SHOW_MS } from "../../constants/global";

import "../../styles/MenusContainer.css";

const animationEffectsCount = 4;
const notifyShowAfterHideDelay = 750;

const MenusContainer = ({
  timeResults,
  inPreparation,
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
    if (inPreparation) {
      notify.show("Preparing simulation...", "custom", -1, NOTIFY_INFO_COLOR);
    } else if (wasPrepared) {
      notify.hide();
      setTimeout(() => {
        notify.show("Simulation prepared!", "success", NOTIFY_SHOW_MS);
      }, notifyShowAfterHideDelay);
    }
  }, [inPreparation]);

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

  useEffect(createEffect(generatePedestrians, [setLimitsClassName]), [generatePedestrians]);
  useEffect(createEffect(generateCars, [setLimitsClassName]), [generateCars]);
  useEffect(createEffect(generateBikes, [setLimitsClassName]), [generateBikes]);
  useEffect(createEffect(generateTroublePoints, [setLimitsClassName]), [generateTroublePoints]);

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
  const { timeResults, inPreparation, wasPrepared, wasStarted } = state.message;
  const {
    startSimulationData: { generateCars, generateBikes, generateTroublePoints },
    prepareSimulationData: { generatePedestrians },
  } = state.interaction;

  return {
    timeResults,
    inPreparation,
    wasPrepared,
    wasStarted,
    generatePedestrians,
    generateCars,
    generateBikes,
    generateTroublePoints,
  };
};

export default connect(mapStateToProps)(React.memo(MenusContainer));
