package osmproxy.buses.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ApiKeyValue {
    public final String key;
    public final String value;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public ApiKeyValue(@JsonProperty("key") String key,
                       @JsonProperty("value") String value) {
        this.key = key;
        this.value = value;
    }
}
