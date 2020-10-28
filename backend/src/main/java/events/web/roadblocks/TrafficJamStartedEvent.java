package events.web.roadblocks;

public class TrafficJamStartedEvent {
    public final int lightId;

    public TrafficJamStartedEvent(int lightId) {this.lightId = lightId;}
}
