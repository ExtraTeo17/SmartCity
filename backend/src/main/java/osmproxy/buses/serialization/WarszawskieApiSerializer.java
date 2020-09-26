package osmproxy.buses.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import osmproxy.buses.models.ApiResult;
import osmproxy.buses.models.SingleTimetable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WarszawskieApiSerializer {
    private final static Logger logger = LoggerFactory.getLogger(WarszawskieApiSerializer.class);
    private final ObjectMapper objectMapper;

    @Inject
    WarszawskieApiSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<SingleTimetable> serializeTimetables(String jsonString) {
        var apiResult = serializeApiResult(jsonString);
        if (apiResult.isEmpty()) {
            return new ArrayList<>();
        }

        var result = new ArrayList<SingleTimetable>();

        return result;
    }

    @VisibleForTesting
    Optional<ApiResult> serializeApiResult(String jsonString) {
        ApiResult apiResult;
        try {
            apiResult = objectMapper.readValue(jsonString, ApiResult.class);
        } catch (IOException e) {
            logger.warn("Failed to serialize json timetables!", e);
            return Optional.empty();
        }

        return Optional.of(apiResult);
    }
}
