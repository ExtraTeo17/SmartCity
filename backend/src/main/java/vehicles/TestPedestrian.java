package vehicles;

import com.google.common.annotations.VisibleForTesting;
import smartcity.ITimeProvider;
import vehicles.enums.DrivingState;
import vehicles.enums.VehicleType;

import java.time.LocalDateTime;


/**
 * The pedestrian for which measurements of its travel time shall be performed.
 */
public class TestPedestrian extends Pedestrian implements ITestable {
    private LocalDateTime start;
    private LocalDateTime end;

    @VisibleForTesting
    public TestPedestrian(ITimeProvider timeProvider) {
        super(timeProvider);
    }

    public TestPedestrian(Pedestrian pedestrian) {
        super(pedestrian);
    }

    public TestPedestrian(Pedestrian pedestrian, DrivingState state, LocalDateTime startSaved, int savedDist) {
        super(pedestrian);
        super.setState(state);
        start = startSaved;
        super.appendDistance(savedDist);
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
        return VehicleType.TEST_PEDESTRIAN.toString();
    }

    @Override
    public LocalDateTime getStart() {
        return start;
    }

    @Override
    public LocalDateTime getEnd() {
        return end;
    }
}
