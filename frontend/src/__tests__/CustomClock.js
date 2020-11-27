import React from "react";
import { render, unmountComponentAtNode } from "react-dom";
import { act } from "react-dom/test-utils";
import { mount } from "../enzyme";
import { CustomClockObj } from "../components/Menu/Main/CustomClock";

let container = null;
beforeEach(() => {
  // setup a DOM element as a render target
  container = document.createElement("div");
  document.body.appendChild(container);
});

afterEach(() => {
  // cleanup on exiting
  unmountComponentAtNode(container);
  container.remove();
  container = null;
});

const time = new Date("2021-02-05T12:00:00Z");
it("Does not start if simulation not started", () => {
  act(() => {
    render(<CustomClockObj wasStarted={false} time={time} timeScale={1} />, container);
  });
  expect(container.textContent).toBe("05.02.202113:00:00");
});

const props = { wasStarted: true, time, timeScale: 1 };
it("Starts if simulation started", () => {
  const wrapper = mount(<CustomClockObj wasStarted={false} time={time} timeScale={1} />);

  const clock = wrapper.find("#clock");
  expect(clock.text()).toBe("05.02.202113:00:00");

  act(() => {
    wrapper.setProps(props);
  });
  expect(clock.text()).toBe("05.02.202113:00:00");
});
