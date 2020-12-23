import React from "react";
import { render, unmountComponentAtNode } from "react-dom";
import { act } from "react-dom/test-utils";
import { mount } from "enzyme";
import { CustomClockObj, timeUpdateScaledThresholdMs, timeUpdateThresholdMs } from "../components/Menu/Main/CustomClock";

const timeScaledGreaterThreshold = timeUpdateScaledThresholdMs + 1;
const timeGreaterThresholdMs = timeUpdateThresholdMs + 1;

let container = null;
let currentTimeFrame;
let frameInterval = 100;
// 2 intervals will pass, because we set time after first frame
let maxFrameTimeElapsed = timeScaledGreaterThreshold * 2 + frameInterval + 1;
beforeEach(() => {
  container = document.createElement("div");
  document.body.appendChild(container);
  currentTimeFrame = 0;
  jest.spyOn(window, "requestAnimationFrame").mockImplementation(cb => {
    currentTimeFrame += frameInterval;
    if (currentTimeFrame < maxFrameTimeElapsed) {
      return cb(currentTimeFrame);
    }
    return () => {};
  });
});

afterEach(() => {
  unmountComponentAtNode(container);
  container.remove();
  container = null;
  jest.clearAllMocks();
});

const initTime = new Date("2021-02-05T12:00:00Z");
const timeStringLocale = "05.02.202113:00:00";

const zeroPad = (num, places) => String(num).padStart(places, "0");
const getTimeStringAfterStarted = (scale = 1) => {
  const cutLastTwo = timeStringLocale.slice(0, -2);
  const scaledTimeSec = parseInt((scale * maxFrameTimeElapsed) / 1000);
  return cutLastTwo + zeroPad(scaledTimeSec, 2);
};

describe("Clock", () => {
  it("does not start if simulation not started", () => {
    act(() => {
      render(<CustomClockObj wasStarted={false} time={initTime} timeScale={1} />, container);
    });
    expect(container.textContent).toBe(timeStringLocale);
  });

  it("starts after simulation started", () => {
    const wrapper = mount(<CustomClockObj wasStarted={false} time={initTime} timeScale={1} />);

    const clock = wrapper.find("#clock");
    expect(clock.text()).toBe(timeStringLocale);

    act(() => {
      wrapper.setProps({ wasStarted: true });
    });
    expect(clock.text()).toBe(getTimeStringAfterStarted());
  });

  it("uses time scale", () => {
    const scale = 5;
    const wrapper = mount(<CustomClockObj wasStarted time={initTime} timeScale={scale} />);

    const clock = wrapper.find("#clock");
    expect(clock.text()).toBe(getTimeStringAfterStarted(scale));
  });

  it("does not update if scaled refresh interval didn't pass", () => {
    const scale = 1;
    frameInterval = timeScaledGreaterThreshold / 10;
    maxFrameTimeElapsed = timeScaledGreaterThreshold * 1.5;
    expect(maxFrameTimeElapsed - frameInterval).toBeGreaterThan(timeScaledGreaterThreshold);

    const wrapper = mount(<CustomClockObj wasStarted time={initTime} timeScale={scale} />);

    const clock = wrapper.find("#clock");
    expect(clock.text()).toBe(getTimeStringAfterStarted(scale));
  });

  it("does not update if normal refresh interval didn't pass", () => {
    const scale = 30;
    maxFrameTimeElapsed = timeGreaterThresholdMs * 0.75;
    frameInterval = maxFrameTimeElapsed / 10;
    expect((maxFrameTimeElapsed - frameInterval) * scale).toBeGreaterThan(timeScaledGreaterThreshold);

    const wrapper = mount(<CustomClockObj wasStarted time={initTime} timeScale={scale} />);

    const clock = wrapper.find("#clock");
    expect(clock.text()).toBe(timeStringLocale);
  });
});
