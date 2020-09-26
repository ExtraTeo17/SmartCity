package osmproxy.buses.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

public class ApiValues implements Iterable<ApiKeyValue> {
    public final List<ApiKeyValue> values;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public ApiValues(@JsonProperty("values") List<ApiKeyValue> values) {
        this.values = values;
    }

    @NotNull
    @Override
    public Iterator<ApiKeyValue> iterator() {
        return values.iterator();
    }
}
