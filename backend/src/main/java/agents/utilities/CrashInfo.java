package agents.utilities;

import java.time.LocalTime;

public class CrashInfo {
    public final LocalTime timestamp;
    public final String busLine;
    public final String brigade;

    public CrashInfo(LocalTime timestamp,
                     String busLine,
                     String brigade) {
        this.timestamp = timestamp;
        this.busLine = busLine;
        this.brigade = brigade;

    }
}
