function getRandomInt(min, max) {
  // eslint-disable-next-line no-bitwise
  return (Math.random() * (max - min + 1) + min) | 0;
}

// https://stackoverflow.com/a/5365036/6841224

export const generateRandomColor = () => {
  const colorDigits = new Array(6);
  let result = 0;
  for (let i = 0; i < 6; ++i) {
    if (i % 2 === 1) {
      colorDigits[i] = getRandomInt(0, 7);
    } else {
      colorDigits[i] = getRandomInt(0, 0xf);
    }
    result |= colorDigits[i] << (4 * i);
  }

  return `#${result.toString(16)}`;
};

const precision = 100; // lat and long precsion, boost to 1000 if need be
const latOffset = 1000; // Anithing above 180 would do

export const getLocationHash = loc => {
  return Number(loc.lat * precision * latOffset) + Number(loc.lng * precision);
};
