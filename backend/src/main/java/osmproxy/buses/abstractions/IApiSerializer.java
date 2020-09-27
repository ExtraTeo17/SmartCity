package osmproxy.buses.abstractions;

import osmproxy.buses.models.TimetableRecord;

import java.util.List;

public interface IApiSerializer {
    List<TimetableRecord> serializeTimetables(String jsonString);
}
