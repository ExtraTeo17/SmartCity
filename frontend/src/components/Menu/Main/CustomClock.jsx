import React, { useEffect, useState } from "react";
import { connect } from "react-redux";
import "../../../styles/CustomClock.css";

/**
 *  Main menu tab, contains:
 *  - zone data, i.e. coordinates and radius
 *  - time and time-scale of simulation
 *  - use-fixed-routes checkbox - routes stay the same for each re-run at the same position
 * @category Menu
 * @module MainMenu
 */

export const timeUpdateScaledThresholdMs = 999;
export const timeUpdateThresholdMs = 49;

const dateFormat = new Intl.DateTimeFormat("pl-PL", {
  year: "numeric",
  month: "2-digit",
  day: "2-digit",
});
const timeFormat = new Intl.DateTimeFormat("pl-PL", {
  hour: "2-digit",
  minute: "2-digit",
  second: "2-digit",
});

/**
 * @typedef {Object} Props - CustomClock parameters
 * @prop {boolean} wasStarted - if simulation was started
 * @prop {Date} time - initial clock time
 * @prop {number} timeScale - time scale of the clock, i.e. clock will change `timeScale` seconds each 1 second
 */

/**
 * Animated clock component shown after start of simulation <br/>
 * resources: {@link https://css-tricks.com/using-requestanimationframe-with-react-hooks/}
 * @class CustomClock
 * @param {Props} props
 */
export const CustomClockObj = props => {
  const { wasStarted, time, timeScale } = props;
  const [currTime, setCurrTime] = useState(time);

  const requestRef = React.useRef(0);
  const previousTimeRef = React.useRef(0);

  useEffect(() => {
    setCurrTime(time);
  }, [time]);

  useEffect(() => {
    if (wasStarted) {
      const animate = time => {
        if (!previousTimeRef.current) {
          previousTimeRef.current = time;
          requestRef.current = requestAnimationFrame(animate);
          return;
        }

        const deltaTime = time - previousTimeRef.current;
        const scaledDeltaTime = timeScale * deltaTime;
        if (scaledDeltaTime > timeUpdateScaledThresholdMs && deltaTime > timeUpdateThresholdMs) {
          setCurrTime(prevTime => new Date(prevTime.getTime() + scaledDeltaTime));
          previousTimeRef.current = time;
        }

        requestRef.current = requestAnimationFrame(animate);
      };

      requestRef.current = requestAnimationFrame(animate);

      return () => cancelAnimationFrame(requestRef.current);
    }
    return () => {};
  }, [wasStarted, timeScale]); // Make sure the effect runs only once

  return (
    <div className="center-wrapper mt-4">
      <div id="clock" className="ml-4">
        <div className="date">{dateFormat.format(currTime)}</div>
        <div className="time">{timeFormat.format(currTime)}</div>
      </div>
    </div>
  );
};

const mapStateToProps = (state /* , ownProps */) => {
  const { wasStarted } = state.message;
  const { startTime, timeScale } = state.interaction.startSimulationData;
  return {
    wasStarted,
    time: startTime,
    timeScale,
  };
};

export default connect(mapStateToProps)(React.memo(CustomClockObj));
