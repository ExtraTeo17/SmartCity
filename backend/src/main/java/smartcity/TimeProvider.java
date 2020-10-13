package smartcity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

// TODO: Realtime or fixed? IMPORTANT.
public class TimeProvider implements ITimeProvider {
    public static final int TIME_SCALE = 12;
    private static final Logger logger = LoggerFactory.getLogger(TimeProvider.class);

    private LocalDateTime simulationStartTime;
    private long realStartTimeNano;

    TimeProvider() {
        simulationStartTime = LocalDateTime.now();
        realStartTimeNano = System.nanoTime();
    }

    @Override
    public LocalDateTime getCurrentSimulationTime() {
        var delta = System.nanoTime() - realStartTimeNano;
        return simulationStartTime.plusNanos(TIME_SCALE * delta);
    }

    @Override
    public void setSimulationStartTime(LocalDateTime simulationTime) {
        this.simulationStartTime = simulationTime;
        this.realStartTimeNano = System.nanoTime();
    }

    @Override
    public LocalDateTime getStartSimulationTime() {
        return simulationStartTime;
    }

    @Override
    public long getNanoStartTime() {
        return realStartTimeNano;
    }

    public static long getTimeInMs(long timeNanoStart) {
        return (System.nanoTime() - timeNanoStart) / 1_000_000;
    }

    public static LocalDateTime getCloser(LocalDateTime source, LocalDateTime a, LocalDateTime b) {
        var diffA = Math.abs(source.until(a, ChronoUnit.MILLIS));
        var diffB = Math.abs(source.until(b, ChronoUnit.MILLIS));

        return diffA < diffB ? a : b;
    }
}
