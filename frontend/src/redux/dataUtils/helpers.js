export const getResultObj = (type, { id, travelTime, travelDistance }) => {
  const minutes = (travelTime / 60) | 0;
  const seconds = (travelTime - minutes * 60) | 0;
  return {
    id,
    type,
    timeTotal: travelTime,
    time: {
      minutes,
      seconds,
    },
    distance: travelDistance,
  };
};
