package smartcity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

// TODO: Realtime or fixed? IMPORTANT.
public class TimeProvider implements ITimeProvider {
    public static final int TIME_SCALE = 15;
    public static final int MS_PER_TICK = 50;
    private static final Logger logger = LoggerFactory.getLogger(TimeProvider.class);

    private LocalDateTime simulationStartTime;
    private long ticks;

    TimeProvider() {
        simulationStartTime = LocalDateTime.now();
        ticks = 0;
    }

    @Override
    public LocalDateTime getCurrentSimulationTime() {
        var delta = ticks * MS_PER_TICK;
        return simulationStartTime.plus(TIME_SCALE * delta, ChronoUnit.MILLIS);
    }

    @Override
    public void setSimulationStartTime(LocalDateTime simulationTime) {
        this.simulationStartTime = simulationTime;
        this.ticks = 0;
    }

    @Override
    public LocalDateTime getStartSimulationTime() {
        return simulationStartTime;
    }

    @Override
    public long getTicks() {
        return ticks;
    }

    @Override
    public Runnable getUpdateTimeTask(long initialTicks) {
        ticks = initialTicks;
        return () -> ++ticks;
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
