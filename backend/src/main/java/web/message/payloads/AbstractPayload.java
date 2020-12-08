package web.message.payloads;

import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class AbstractPayload {
    @Override
    public String toString() {
        var fields = Arrays.stream(this.getClass().getDeclaredFields())
                .filter(f -> !f.getName().startsWith("this$0"));
        return "(" + fields.map(f -> {
            String result = f.getName() + ": ";
            try {
                f.setAccessible(true);
                var value = f.get(this);
                result += value.toString();
            } catch (IllegalAccessException e) {
                result += "?";
            }
            return result;
        }).collect(Collectors.joining(", ")) + ")";
    }
}
