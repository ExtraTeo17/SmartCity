import { storageKey } from "../../constants/global";

/**
 * @category Redux
 * @module dataUtils
 */

/**
 * @typedef testObjectData
 * @property {Number} id
 * @property {Number} travelTime
 * @property {Number} travelDistance
 */

/**
 * @function
 * @param {String} type - Type of test object
 * @param {testObjectData} resultData - Test object
 */
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

/**
 * Local storage
 * @constant localStorage
 */
export const { localStorage } = window;

/**
 * @typedef {Object} localStorageData
 * @property {module:ApiManager~PrepareSimulationData} prepareSimulationData
 * @property {module:ApiManager~StartSimulationData} startSimulationData
 */

/**
 * Used to create local-storage data from redux state
 * @function
 * @param {Object} state - interaction reducer state
 * @param {module:ApiManager~PrepareSimulationData} state.prepareSimulationData
 * @param {module:ApiManager~StartSimulationData} state.startSimulationData
 * @returns {localStorageData}
 */
export const createLocalDataObject = state => {
  return { prepareSimulationData: state.prepareSimulationData, startSimulationData: state.startSimulationData };
};

/**
 * Used to save data to local storage
 * @function
 * @param {localStorageData} data
 */
export const saveLocalData = data => {
  console.info("Saving data to localStorage");
  localStorage.setItem(storageKey, JSON.stringify(data));
};

/**
 * Used to load data from local storage
 * @function
 * @returns {localStorageData}
 */
export const loadLocalData = () => {
  const localDataString = localStorage.getItem(storageKey);
  if (localDataString) {
    const data = JSON.parse(localDataString);

    const startData = data.startSimulationData;
    const newDate = new Date(startData.startTime);
    const dateToday = new Date();
    if (newDate.getDate() !== dateToday.getDate()) {
      startData.startTime = dateToday;
    } else {
      startData.startTime = newDate;
    }

    return data;
  }

  return null;
};
