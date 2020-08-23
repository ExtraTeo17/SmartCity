package smartcity;

import java.util.Calendar;
import java.util.Date;

public class TimeManager implements ITimeManager {
    public final static int TIME_SCALE = 10;

    private final Calendar calendar;
    private long simulationStartTime;
    private long realStartTime;

    public TimeManager() {
        calendar = Calendar.getInstance();
        simulationStartTime = realStartTime = calendar.getTimeInMillis();
    }

    @Override
    public Date getCurrentSimulationTime() {
        var delta = calendar.getTimeInMillis() - realStartTime;
        return new Date(simulationStartTime + TIME_SCALE * delta);
    }

    @Override
    public Date getCurrentRealTime() {
        return calendar.getTime();
    }

    @Override
    public void setSimulationStartTime(Date simulationTime) {
        simulationStartTime = simulationTime.getTime();
        realStartTime = calendar.getTimeInMillis();
    }

    @Override
    public Date getStartSimulationTime() {
        return new Date(simulationStartTime);
    }

    @Override
    public Date getStartRealTime() {
        return new Date(realStartTime);
    }
}
