package osmproxy.buses.abstractions;

import osmproxy.buses.models.TimetableRecord;

import java.util.List;
//TODO:dokumentacja

public interface IApiSerializer {
    List<TimetableRecord> serializeTimetables(String jsonString);
}
