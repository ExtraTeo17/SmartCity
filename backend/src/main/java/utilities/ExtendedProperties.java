package utilities;

import java.util.Properties;

public class ExtendedProperties extends Properties {
    public <T> T getOrDefault(String key, T defaultValue) {
        //noinspection unchecked
        return (T) super.getOrDefault(key, defaultValue);
    }

    public int getOrDefault(String key, int defaultValue) {
        var value = (String) super.get(key);
        return value != null ? Integer.parseInt(value) : defaultValue;
    }
}
