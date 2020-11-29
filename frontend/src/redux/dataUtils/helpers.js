import { storageKey } from "../../constants/global";

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

export const { localStorage } = window;

export const createLocalDataObject = state => {
  return { prepareSimulationData: state.prepareSimulationData, startSimulationData: state.startSimulationData };
};

export const saveLocalData = data => {
  console.info("Saving data to localStorage");
  localStorage.setItem(storageKey, JSON.stringify(data));
};

export const loadLocalData = () => {
  const localDataString = localStorage.getItem(storageKey);
  if (localDataString) {
    const data = JSON.parse(localDataString);
    data.startSimulationData.startTime = new Date();
    return data;
  }

  return null;
};
