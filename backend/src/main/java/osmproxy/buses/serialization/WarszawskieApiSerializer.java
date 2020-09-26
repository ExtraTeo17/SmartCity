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
import java.util.stream.Collectors;

public class WarszawskieApiSerializer {
    private final static Logger logger = LoggerFactory.getLogger(WarszawskieApiSerializer.class);
    private final ObjectMapper objectMapper;

    @Inject
    WarszawskieApiSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<SingleTimetable> serializeTimetables(String jsonString) {
        var apiResult = serializeJsonString(jsonString);
        if (apiResult.isEmpty()) {
            return new ArrayList<>();
        }

        return serializeApiResult(apiResult.get());
    }

    @VisibleForTesting
    Optional<ApiResult> serializeJsonString(String jsonString) {
        ApiResult apiResult;
        try {
            apiResult = objectMapper.readValue(jsonString, ApiResult.class);
        } catch (IOException e) {
            logger.warn("Failed to serialize json timetables!", e);
            return Optional.empty();
        }

        return Optional.of(apiResult);
    }

    @VisibleForTesting
    List<SingleTimetable> serializeApiResult(ApiResult result) {
        var timetables = new ArrayList<SingleTimetable>();
        for (var apiValue : result) {
            var valuesMap = apiValue.values.stream()
                    .collect(Collectors.toMap(keyValue -> keyValue.key, keyValue -> keyValue.value));
            var record = objectMapper.convertValue(valuesMap, SingleTimetable.class);
            timetables.add(record);
        }

        return timetables;
    }
}
