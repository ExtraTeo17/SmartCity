package events.web.roadblocks;

public class TrafficJamFinishedEvent {
    public final long lightId;

    public TrafficJamFinishedEvent(long lightId) {this.lightId = lightId;}
}
