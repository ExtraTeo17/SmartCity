package smartcity;

public enum SimulationState {
    INITIAL,
    IN_PREPARATION,
    READY_TO_RUN,
    RUNNING,
    FINISHED;

    public boolean isOneOf(SimulationState... states) {
        for (var state : states) {
            if (state.equals(this)) {
                return true;
            }
        }

        return false;
    }
}
