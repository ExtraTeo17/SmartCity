import { createSelector } from "reselect";

const getPedestrians = state => state.message.pedestrians;

export default createSelector([getPedestrians], pedestrians => pedestrians.filter(p => p.hidden !== true));
