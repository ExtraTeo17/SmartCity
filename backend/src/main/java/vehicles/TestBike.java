package vehicles;

import vehicles.enums.DrivingState;
import vehicles.enums.VehicleType;

import java.time.LocalDateTime;

public class TestBike extends Bike implements ITestable {
    private LocalDateTime start;
    private LocalDateTime end;

    public TestBike(Bike bike) {
        super(bike);
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
        return VehicleType.TEST_BIKE.toString();
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
