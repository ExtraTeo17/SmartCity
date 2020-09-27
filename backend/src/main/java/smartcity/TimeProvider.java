package smartcity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

// TODO: Realtime or fixed? IMPORTANT.
public class TimeProvider implements ITimeProvider {
    public static final int TIME_SCALE = 12;
    private static final Logger logger = LoggerFactory.getLogger(TimeProvider.class);

    private final Clock clock;
    private LocalDateTime simulationStartTime;
    private LocalDateTime realStartTime;

    public TimeProvider() {
        clock = Clock.systemDefaultZone();
        simulationStartTime =
                realStartTime =
                        LocalDateTime.now(clock);
    }

    @Override
    public LocalDateTime getCurrentSimulationTime() {
        var delta = Math.abs((long)simulationStartTime.getNano() - realStartTime.getNano());
        return LocalDateTime.now(clock).plusNanos(TIME_SCALE * delta);
    }

    @Override
    public LocalDateTime getCurrentRealTime() {
        return LocalDateTime.now(clock);
    }

    @Override
    public void setSimulationStartTime(LocalDateTime simulationTime) {
        this.simulationStartTime = simulationTime;
        this.realStartTime = LocalDateTime.now(clock);
    }

    @Override
    public LocalDateTime getStartSimulationTime() {
        return simulationStartTime;
    }

    @Override
    public LocalDateTime getStartRealTime() {
        return realStartTime;
    }

    public static long getTimeInMs(long timeNanoStart) {
        return (System.nanoTime() - timeNanoStart) / 1_000_000;
    }
}
