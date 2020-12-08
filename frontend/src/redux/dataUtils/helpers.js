import { storageKey } from "../../constants/global";

/**
 * @category Redux
 * @module helpers
 */

/**
 * @typedef testObjectData
 * @property {Number} id
 * @property {Number} travelTime
 * @property {Number} travelDistance
 */

/**
 *
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
 * @name localStorage
 * Local storage
 */
export const { localStorage } = window;

/**
 * @typedef {Object} localStorageData
 * @property {module:ApiManager~PrepareSimulationData} prepareSimulationData
 * @property {module:ApiManager~StartSimulationData} startSimulationData
 */

/**
 * Used to create local-storage data from redux state
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
 * @param {localStorageData} data
 */
export const saveLocalData = data => {
  console.info("Saving data to localStorage");
  localStorage.setItem(storageKey, JSON.stringify(data));
};

/**
 * Used to load data from local storage
 * @returns {localStorageData}
 */
export const loadLocalData = () => {
  const localDataString = localStorage.getItem(storageKey);
  if (localDataString) {
    const data = JSON.parse(localDataString);
    data.startSimulationData.startTime = new Date();
    return data;
  }

  return null;
};
