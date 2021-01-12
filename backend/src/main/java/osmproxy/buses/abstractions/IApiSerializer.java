package osmproxy.buses.abstractions;

import osmproxy.buses.models.TimetableRecord;

import java.util.List;

/**
 * Serialises output of request to API
 */
public interface IApiSerializer {
    List<TimetableRecord> serializeTimetables(String jsonString);
}
