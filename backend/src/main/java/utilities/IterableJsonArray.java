package utilities;

import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Iterator;

public class IterableJsonArray<T> implements Iterable<T> {
    private final JSONArray jsonArray;
    private final Class<T> type;

    private IterableJsonArray(JSONArray array, Class<T> type) {
        this.jsonArray = array;
        this.type = type;
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private int iter = 0;

            @Override
            public boolean hasNext() {
                return iter < jsonArray.size();
            }

            @Override
            public T next() {
                return type.cast(jsonArray.get(iter++));
            }
        };
    }

    public static <TArray> IterableJsonArray<TArray> of(JSONArray jsonArray, Class<TArray> type) {
        return new IterableJsonArray<TArray>(jsonArray, type);
    }

    public static <TArray> IterableJsonArray<TArray> of(Object jsonArray, Class<TArray> type) {
        return IterableJsonArray.of((JSONArray)jsonArray, type);
    }

    public static <TArray> IterableJsonArray<TArray> of(JSONObject jsonObject, String key, Class<TArray> type) {
        return IterableJsonArray.of(jsonObject.get(key), type);
    }

    public static IterableJsonArray<JSONObject> of(JSONObject jsonObject, String key) {
        return IterableJsonArray.of(jsonObject.get(key), JSONObject.class);
    }
}
