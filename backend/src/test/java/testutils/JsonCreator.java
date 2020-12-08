package testutils;

public class JsonCreator {
    static String createArray(String name, String... objects) {
        return createArrayProperty(name, createArray(objects));
    }

    private static String createArray(String... objects) {
        if (objects.length == 0) {
            return "[]";
        }

        var builder = new StringBuilder("[" + objects[0]);
        for (int i = 1; i < objects.length; ++i) {
            builder.append(",").append(objects[i]);
        }
        builder.append("]");

        return builder.toString();
    }

    static String createObject(String... properties) {
        if (properties.length == 0) {
            return "{}";
        }

        var builder = new StringBuilder("{" + properties[0]);
        for (int i = 1; i < properties.length; ++i) {
            builder.append(",").append(properties[i]);
        }
        builder.append("}");

        return builder.toString();
    }

    static String createProperty(String key, String value) {
        return "\"" + key + "\":" + "\"" + value + "\"";
    }

    private static String createArrayProperty(String key, String array) {
        return "\"" + key + "\":" + array;
    }
}
