package events.web.roadblocks;

public class TrafficJamFinishedEvent {
    public final int lightId;

    public TrafficJamFinishedEvent(int lightId) {this.lightId = lightId;}
}
