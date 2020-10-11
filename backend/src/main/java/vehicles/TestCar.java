package vehicles;

import com.google.common.annotations.VisibleForTesting;
import smartcity.ITimeProvider;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class TestCar extends Car {
    private final ITimeProvider timeProvider;

    private LocalDateTime start;
    private LocalDateTime end;

    public TestCar(Car movingObject,
                   ITimeProvider timeProvider) {
        super(movingObject);
        this.timeProvider = timeProvider;
    }

    @VisibleForTesting
    TestCar(ITimeProvider timeProvider) {
        super(new ArrayList<>(), new ArrayList<>());
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
        }

        super.setState(newState);
    }

    @Override
    public String getVehicleType() {
        return VehicleType.TEST_CAR.toString();
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }
}
