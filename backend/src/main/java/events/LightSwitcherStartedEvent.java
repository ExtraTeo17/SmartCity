package events;

public class LightSwitcherStartedEvent {
    public final int managerId;
    public final int defaultExecutionDelay;

    public LightSwitcherStartedEvent(int managerId, int defaultExecutionDelay) {
        this.managerId = managerId;
        this.defaultExecutionDelay = defaultExecutionDelay;
    }
}
