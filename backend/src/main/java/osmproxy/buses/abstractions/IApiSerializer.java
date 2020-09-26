package osmproxy.buses.abstractions;

import osmproxy.buses.models.SingleTimetable;

import java.util.List;

public interface IApiSerializer {
    List<SingleTimetable> serializeTimetables(String jsonString);
}
