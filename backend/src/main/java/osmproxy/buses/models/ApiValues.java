package osmproxy.buses.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ApiValues {
    public final List<ApiKeyValue> values;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public ApiValues(@JsonProperty("values") List<ApiKeyValue> values) {
        this.values = values;
    }
}
