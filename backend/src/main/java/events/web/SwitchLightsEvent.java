package events.web;

public class SwitchLightsEvent {
    public final long osmLightId;

    public SwitchLightsEvent(long osmLightId) {this.osmLightId = osmLightId;}
}