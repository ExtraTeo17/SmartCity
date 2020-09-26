package osmproxy.buses.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ApiResult {
    public final List<ApiValues> apiValues;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public ApiResult(@JsonProperty("result") List<ApiValues> apiValues) {
        this.apiValues = apiValues;
    }
}
