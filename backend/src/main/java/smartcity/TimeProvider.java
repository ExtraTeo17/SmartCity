package smartcity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.util.Date;

public class TimeProvider implements ITimeProvider {
    public static final int TIME_SCALE = 10;
    private static final Logger logger = LoggerFactory.getLogger(TimeProvider.class);

    private final Clock clock;
    private long simulationStartTime;
    private long realStartTime;

    public TimeProvider() {
        clock = Clock.systemDefaultZone();
        simulationStartTime = realStartTime = clock.millis();
    }

    @Override
    public Date getCurrentSimulationTime() {
        var delta = clock.millis() - realStartTime;
        return new Date(simulationStartTime + TIME_SCALE * delta);
    }

    @Override
    public Date getCurrentRealTime() {
        return new Date(clock.millis());
    }

    @Override
    public void setSimulationStartTime(Date simulationTime) {
        simulationStartTime = simulationTime.getTime();
        realStartTime = clock.millis();
    }

    @Override
    public Date getStartSimulationTime() {
        return new Date(simulationStartTime);
    }

    @Override
    public Date getStartRealTime() {
        return new Date(realStartTime);
    }

    public static long getTimeInMs(long timeNanoStart) {
        return (System.nanoTime() - timeNanoStart) / 1_000_000;
    }
}
