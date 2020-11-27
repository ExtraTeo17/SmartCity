import React, { useEffect, useState } from "react";
import { connect } from "react-redux";
import "../../../styles/CustomClock.css";

const timeUpdateThresholdMs = 1000;
const dateFormat = new Intl.DateTimeFormat("pl-PL", {
  dateStyle: "short",
});
const timeFormat = new Intl.DateTimeFormat("pl-PL", {
  timeStyle: "medium",
});

// https://css-tricks.com/using-requestanimationframe-with-react-hooks/
export const CustomClockObj = props => {
  const { wasStarted, time, timeScale } = props;
  const [currTime, setCurrTime] = useState(time);

  const requestRef = React.useRef();
  const previousTimeRef = React.useRef();

  useEffect(() => {
    setCurrTime(time);
  }, [time]);

  useEffect(() => {
    if (wasStarted) {
      const animate = time => {
        if (previousTimeRef.current !== undefined) {
          const deltaTime = time - previousTimeRef.current;
          const scaledDeltaTime = timeScale * deltaTime;
          if (scaledDeltaTime > timeUpdateThresholdMs) {
            setCurrTime(prevTime => new Date(prevTime.getTime() + scaledDeltaTime));
            previousTimeRef.current = time;
          }
        } else {
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
