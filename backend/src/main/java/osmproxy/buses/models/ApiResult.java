package osmproxy.buses.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

public class ApiResult implements Iterable<ApiValues> {
    public final List<ApiValues> apiValues;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public ApiResult(@JsonProperty("result") List<ApiValues> apiValues) {
        this.apiValues = apiValues;
    }

    @NotNull
    @Override
    public Iterator<ApiValues> iterator() {
        return apiValues.iterator();
    }
}
