package events.web.roadblocks;

public class TrafficJamStartedEvent {
    public final long lightId;

    public TrafficJamStartedEvent(long lightId) {this.lightId = lightId;}
}
