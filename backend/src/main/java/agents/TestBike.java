package agents;

import com.google.common.annotations.VisibleForTesting;
import smartcity.ITimeProvider;
import vehicles.Bike;
import vehicles.Car;
import vehicles.ITestable;
import vehicles.Pedestrian;
import vehicles.enums.DrivingState;
import vehicles.enums.VehicleType;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class TestBike extends Bike implements ITestable {
    private LocalDateTime start;
    private LocalDateTime end;
    ITimeProvider timeProvider;

    public TestBike(Bike bike, ITimeProvider timeProvider) {
        super(bike);
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
