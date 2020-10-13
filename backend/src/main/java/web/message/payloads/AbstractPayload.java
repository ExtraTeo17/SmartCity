package web.message.payloads;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class AbstractPayload {
    @Override
    public String toString() {
        Field[] fields = this.getClass().getFields();
        return "(" + Arrays.stream(fields).map(f -> {
            String result = f.getName() + ": ";
            try {
                var value = f.get(this);
                result += value.toString();
            } catch (IllegalAccessException e) {
                result += "?";
            }
            return result;
        }).collect(Collectors.joining(", ")) + ")";
    }
}
