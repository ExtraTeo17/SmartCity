package vehicles;

import com.google.common.annotations.VisibleForTesting;
import smartcity.ITimeProvider;

import java.time.LocalDateTime;

public class TestPedestrian extends Pedestrian {
    private final ITimeProvider timeProvider;

    private LocalDateTime start;
    private LocalDateTime end;

    @VisibleForTesting
    TestPedestrian(ITimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    public TestPedestrian(Pedestrian pedestrian, ITimeProvider timeProvider) {
        super(pedestrian);
        this.timeProvider = timeProvider;
    }

    @Override
    public void setState(DrivingState newState) {
        var initialState = getState();
        if (initialState == DrivingState.STARTING) {
            start = timeProvider.getCurrentSimulationTime();
        }
        if (newState == DrivingState.AT_DESTINATION) {
            end = timeProvider.getCurrentSimulationTime();
            ;
        }

        super.setState(newState);
    }

    @Override
    public String getVehicleType() {
        return VehicleType.TEST_PEDESTRIAN.toString();
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }
}
