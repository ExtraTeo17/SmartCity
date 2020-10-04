package agents.utilities;

import vehicles.MovingObject;
import vehicles.VehicleType;

public class MessageParameter {
    public static final String TYPE = "type";
    public static final String VEHICLE = "vehicle";
    public static final String BUS = "bus";
    public static final String STATION = "station";
    public static final String LIGHT = "light";
    public static final String PEDESTRIAN = "pedestrian";
    public static final String ADJACENT_OSM_WAY_ID = "adjacent_osm_way_id";
    public static final String ARRIVAL_TIME = "arrival_time";
    public static final String AT_DESTINATION = "at_destination";
    public static final String BUS_AGENT_NAME = "bus_id";
    public static final String STATION_ID = "station_id";
    public static final String SCHEDULE_ARRIVAL = "schedule_arrival";
    public static final String DESIRED_BUS_LINE = "desired_bus";
    public static final String BUS_LINE = "bus_line";

    public static String getTypeByMovingObject(MovingObject movingObject) {
        var type = movingObject.getVehicleType();
        var value = VehicleType.getValue(type);
        return switch (value) {
            case BUS -> BUS;
            case PEDESTRIAN, TEST_PEDESTRIAN -> PEDESTRIAN;
            case REGULAR_CAR, TEST_CAR -> VEHICLE;
        };
    }
}
