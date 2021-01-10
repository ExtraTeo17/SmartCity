package agents.utilities;

import vehicles.MovingObject;
import vehicles.enums.VehicleType;

public class MessageParameter {
    public static final String TYPE = "type";
    public static final String VEHICLE = "vehicle";
    public static final String TROUBLE_MANAGER = "trouble_manager";
    public static final String BUS = "bus";
    public static final String BIKE = "bike";
    public static final String STATION = "station";
    public static final String LIGHT = "light";
    public static final String PEDESTRIAN = "pedestrian";
    public static final String ADJACENT_OSM_WAY_ID = "adjacent_osm_way_id";
    public static final String ARRIVAL_TIME = "arrival_time";
    public static final String AT_DESTINATION = "at_destination";
    public static final String BUS_AGENT_NAME = "bus_id";
    public static final String STATION_ID = "station_id";
    public static final String STATION_FROM_ID = "station_from_id";
    public static final String STATION_TO_ID = "station_to_id";
    public static final String BUS_MANAGER = "bus_manager";

    public static final String SCHEDULE_ARRIVAL = "schedule_arrival";
    public static final String BUS_LINE = "bus_line";
    public static final String TROUBLE_LON = "trouble_lon";
    public static final String TROUBLE_LAT = "trouble_lat";
    public static final String EDGE_ID = "edge_id";
    public static final String TROUBLE = "trouble";
    public static final String TYPEOFTROUBLE = "type_of_trouble";
    public static final String CRASH = "crash";
    public static final String CONSTRUCTION = "construction";
    public static final String ACCIDENT = "accident";
    public static final String TRAFFIC_JAM = "traffic_jams";
    public static final String SHOW = "show";
    public static final String STOP = "stop";
    public static final String LENGTH_OF_JAM = "length_of_jam";

    public static final String DESIRED_OSM_STATION_ID = "desired_osm_station_id";
    public static final String OSM_ID_OF_NEXT_CLOSEST_STATION = "osm_id_of_next_closest_station";
    public static final String AGENT_ID_OF_NEXT_CLOSEST_STATION = "agent_id_of_next_closest_station";
    public static final String LAT_OF_NEXT_CLOSEST_STATION = "lat_of_next_closest_station";
    public static final String LON_OF_NEXT_CLOSEST_STATION = "lon_of_next_closest_station";
    public static final String TIME = "time";
    public static final String TIME_BETWEEN_PEDESTRIAN_AT_STATION_ARRIVAL_AND_REACHING_DESIRED_STOP = "time__desired_stop";
    public static final String EVENT = "event";
    public static final String START = "start";
    public static final String BRIGADE = "brigade";
    public static final String TEST_BIKE_AGENT_ID = "test_bike_agent_id";
    public static final String CRASH_TIME = "crash_time";

    /**
     * Determine the subtype of the moving object agent.
     *
     * @param movingObject The agent for which the type is to be determined
     * @return The string representation of the moving object supertype 
     */
    public static String getTypeByMovingObject(MovingObject movingObject) {
        var type = movingObject.getVehicleType();
        var value = VehicleType.getValue(type);
        return switch (value) {
            case BIKE, TEST_BIKE -> BIKE;
            case BUS -> BUS;
            case PEDESTRIAN, TEST_PEDESTRIAN -> PEDESTRIAN;
            case REGULAR_CAR, TEST_CAR -> VEHICLE;
        };
    }
}
