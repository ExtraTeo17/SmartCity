export const StartState = {
  Initial: 200,
  Invoke: 300,
  Proceed: 400,
};

export const getNextStartState = oldState => {
  switch (oldState) {
    case StartState.Initial:
    case StartState.Replace:
      return StartState.Invoke;

    case StartState.Invoke:
      return StartState.Proceed;

    default:
      return StartState.Initial;
  }
};

export const ConfigState = {
  Initial: 1000,
  Replace_Start: 2000,
  Replace_Restart: 3000,
};

export const getNextConfigState = oldState => {
  switch (oldState) {
    case ConfigState.Initial:
      return ConfigState.Replace_Start;

    case ConfigState.Replace_Start:
      return ConfigState.Replace_Restart;

    case ConfigState.Replace_Restart:
      return ConfigState.Replace_Start;

    default:
      return ConfigState.Initial;
  }
};
