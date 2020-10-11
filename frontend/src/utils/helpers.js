// https://stackoverflow.com/a/5365036/6841224
export const generateRandomColor = () => {
  return "#" + ((Math.random() * 0xffffff) << 0).toString(16);
};
