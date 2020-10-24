import React from "react";
import Tabs from "react-bootstrap/Tabs";
import Tab from "react-bootstrap/Tab";
import Menu from "./Menu";
import "../../styles/MenusContainer.css";

const MenusContainer = props => {
  return (
    <Tabs defaultActiveKey="main" variant="pills" id="tabbed-menu">
      <Tab eventKey="main" title="Main">
        <Menu />
      </Tab>
      <Tab eventKey="strategy" title="Strategy">
        <div>Strategy settings</div>
      </Tab>
      <Tab eventKey="contact" title="Setups" disabled>
        <div>Predefined setups (ex. Test Bus Zone) will be here</div>
      </Tab>
      <Tab eventKey="results" title="Results" disabled>
        <div>List of test results will be here</div>
      </Tab>
    </Tabs>
  );
};

export default MenusContainer;
