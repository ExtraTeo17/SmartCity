import { CENTER_UPDATED } from "./constants";

export const centerUpdated = center => {
  return {
    type: CENTER_UPDATED,
    payload: {
      center,
    },
  };
};
